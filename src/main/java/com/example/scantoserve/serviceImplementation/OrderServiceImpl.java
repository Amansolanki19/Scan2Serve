package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.*;
import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.BadRequestException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.*;
import com.example.scantoserve.service.OrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final CustomerOrderRepo customerOrderRepo;
    private final OrderItemRepo orderItemRepo;
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final DiningTableRepo diningTableRepo;
    private final UserRepo userRepo;
    private final RestaurantRepo restaurantRepo;

    // Identify the currently logged-in customer
    private User getCurrentCustomer() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails userDetails)) {

            throw new BadRequestException("Customer authentication is required");
        }

        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getRestaurantOrders(Long restaurantId) {

        getAuthorizedRestaurant(restaurantId);

        return customerOrderRepo
                .findAllByRestaurant_IdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getRestaurantOrdersByStatus(
            Long restaurantId,
            OrderStatus status
    ) {

        getAuthorizedRestaurant(restaurantId);

        return customerOrderRepo
                .findAllByRestaurant_IdAndOrderStatusOrderByCreatedAtDesc(
                        restaurantId,
                        status
                )
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    public OrderResponseDto updateOrderStatus(
            Long restaurantId,
            Long orderId,
            UpdateOrderStatusRequestDto request
    ) {

        getAuthorizedRestaurant(restaurantId);

        CustomerOrder order = customerOrderRepo
                .findByIdAndRestaurant_Id(orderId, restaurantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        OrderStatus currentStatus = order.getOrderStatus();
        OrderStatus newStatus = request.orderStatus();

        boolean validTransition = switch (currentStatus) {
            case PLACED -> newStatus == OrderStatus.CONFIRMED
                    || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED -> newStatus == OrderStatus.PREPARING
                    || newStatus == OrderStatus.CANCELLED;

            case PREPARING -> newStatus == OrderStatus.READY;

            case READY -> newStatus == OrderStatus.SERVED;

            case SERVED -> newStatus == OrderStatus.COMPLETED;

            case COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new BadRequestException(
                    "Invalid order status transition from "
                            + currentStatus + " to " + newStatus);
        }

        order.setOrderStatus(newStatus);

        CustomerOrder savedOrder = customerOrderRepo.save(order);

        return mapToOrderResponse(savedOrder);
    }

    @Transactional
    @Override
    public OrderResponseDto checkout(CheckoutRequestDto request) {

        User customer = getCurrentCustomer();

        // 1. Find dining table using QR token
        DiningTable table = diningTableRepo.findByQrToken(request.qrToken())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invalid QR code"));

        if (table.getStatus() == DiningTableStatus.INACTIVE) {
            throw new BadRequestException("Dining table is inactive");
        }

        if (table.getRestaurant().getStatus() != RestaurantStatus.ACTIVE) {
            throw new BadRequestException("Restaurant is inactive");
        }

        // 2. Find customer's active cart
        Cart cart = cartRepo
                .findByCustomer_IdAndDiningTable_IdAndStatus(
                        customer.getId(),
                        table.getId(),
                        CartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Active cart not found"));

        List<CartItem> cartItems =
                cartItemRepo.findAllByCart_Id(cart.getId());

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot checkout an empty cart");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        CustomerOrder order = CustomerOrder.builder()
                .customer(customer)
                .restaurant(table.getRestaurant())
                .diningTable(table)
                .orderStatus(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        for (CartItem cartItem : cartItems) {

            MenuItem menuItem = cartItem.getMenuItem();

            if (menuItem.getStatus() != MenuItemStatus.AVAILABLE) {
                throw new BadRequestException(
                        menuItem.getName() + " is currently unavailable");
            }

            if (menuItem.getCategory().getStatus() != MenuCategoryStatus.ACTIVE) {
                throw new BadRequestException(
                        menuItem.getName() + "'s category is inactive");
            }

            BigDecimal unitPrice = cartItem.getUnitPrice();

            BigDecimal itemSubtotal = unitPrice.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            OrderItem orderItem = OrderItem.builder()
                    .customerOrder(order)
                    .menuItemId(menuItem.getId())
                    .itemName(menuItem.getName())
                    .description(menuItem.getDescription())
                    .unitPrice(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .subtotal(itemSubtotal)
                    .build();

            order.getItems().add(orderItem);

            subtotal = subtotal.add(itemSubtotal);
        }

        BigDecimal totalAmount = subtotal
                .add(taxAmount)
                .subtract(discountAmount);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);

        CustomerOrder savedOrder = customerOrderRepo.save(order);

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepo.save(cart);

        cartItemRepo.deleteAllByCart_Id(cart.getId());

        return mapToOrderResponse(savedOrder);
    }


    // Get order by ID for the logged-in customer
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {

        User customer = getCurrentCustomer();

        CustomerOrder order = customerOrderRepo
                .findByIdAndCustomer_Id(orderId, customer.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getMyOrders(Pageable pageable) {

        User customer = getCurrentCustomer();

        return customerOrderRepo
                .findAllByCustomer_IdOrderByCreatedAtDesc(
                        customer.getId(),
                        pageable
                )
                .map(this::mapToOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getRestaurantOrders(
            Long restaurantId,
            Pageable pageable
    ) {

        getAuthorizedRestaurant(restaurantId);

        return customerOrderRepo
                .findAllByRestaurant_IdOrderByCreatedAtDesc(
                        restaurantId,
                        pageable
                )
                .map(this::mapToOrderResponse);
    }

    // Convert entity into response DTO
    private OrderResponseDto mapToOrderResponse(CustomerOrder order) {

        List<OrderItem> orderItems =
                orderItemRepo.findAllByCustomerOrder_Id(order.getId());

        List<OrderItemResponseDto> itemResponses = orderItems.stream()
                .map(item -> new OrderItemResponseDto(
                        item.getId(),
                        item.getMenuItemId(),
                        item.getItemName(),
                        item.getDescription(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        int totalItems = itemResponses.stream()
                .mapToInt(OrderItemResponseDto::quantity)
                .sum();

        return new OrderResponseDto(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getFullName(),
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                order.getDiningTable().getId(),
                order.getDiningTable().getTableNumber(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                itemResponses,
                totalItems,
                order.getSubtotal(),
                order.getTaxAmount(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMyOrders() {

        User customer = getCurrentCustomer();

        List<CustomerOrder> orders =
                customerOrderRepo.findAllByCustomer_IdOrderByCreatedAtDesc(
                        customer.getId()
                );

        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    private Restaurant getAuthorizedRestaurant(Long restaurantId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new BadRequestException("Authentication is required");
        }

        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return restaurantRepo.findById(restaurantId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Restaurant not found"));
        }

        if (user.getRole() != UserRole.RESTAURANT_ADMIN) {
            throw new BadRequestException(
                    "You are not authorized to access restaurant orders");
        }

        return restaurantRepo.findByIdAndOwner_Id(
                        restaurantId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant not found or access denied"));
    }
}