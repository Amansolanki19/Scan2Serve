
package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.*;
import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.BadRequestException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.*;
import com.example.scantoserve.service.DashboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CustomerOrderRepo orderRepo;
    private final MenuCategoryRepo menuCategoryRepo;
    private final MenuItemRepo menuItemRepo;
    private final PaymentRepo paymentRepo;
    private final DiningTableRepo diningTableRepo;
    private final RestaurantRepo restaurantRepo;
    private final UserRepo userRepo;

    // Get currently authenticated user
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails userDetails)) {

            throw new AccessDeniedException("User is not authenticated");
        }

        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    // Validate restaurant access
    private Restaurant getAuthorizedRestaurant(Long restaurantId) {

        User user = getCurrentUser();

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return restaurantRepo.findById(restaurantId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Restaurant not found"));
        }

        if (user.getRole() != UserRole.RESTAURANT_ADMIN) {
            throw new AccessDeniedException(
                    "Only restaurant administrators can access the dashboard"
            );
        }

        return restaurantRepo.findByIdAndOwner_Id(
                        restaurantId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You do not have access to this restaurant"
                        ));
    }

    @Override
    public DashboardOverviewResponseDto getDashboardOverview(Long restaurantId) {

        Restaurant restaurant = getAuthorizedRestaurant(restaurantId);

        Long id = restaurant.getId();

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay()
                .minusNanos(1);

        BigDecimal totalRevenue = orderRepo.calculateRevenue(
                id,
                PaymentStatus.SUCCESS
        );

        BigDecimal todayRevenue = orderRepo.calculateRevenueBetween(
                id,
                PaymentStatus.SUCCESS,
                startOfDay,
                endOfDay
        );

        return new DashboardOverviewResponseDto(
                orderRepo.countByRestaurant_Id(id),

                orderRepo.countByRestaurant_IdAndCreatedAtBetween(
                        id, startOfDay, endOfDay
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.PLACED
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.COMPLETED
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.CANCELLED
                ),

                safeAmount(totalRevenue),
                safeAmount(todayRevenue),

                orderRepo.countUniqueCustomers(id),

                menuItemRepo.countByCategory_Restaurant_Id(id),

                diningTableRepo.countByRestaurant_Id(id)
        );
    }

    @Override
    public RevenueSummaryResponseDto getRevenueSummary(Long restaurantId) {

        Restaurant restaurant = getAuthorizedRestaurant(restaurantId);

        Long id = restaurant.getId();

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1)
                .atStartOfDay().minusNanos(1);

        LocalDate startOfWeek = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        LocalDateTime startOfWeekDateTime =
                startOfWeek.atStartOfDay();

        LocalDate startOfMonth = today.withDayOfMonth(1);

        LocalDateTime startOfMonthDateTime =
                startOfMonth.atStartOfDay();

        BigDecimal todayRevenue = orderRepo.calculateRevenueBetween(
                id, PaymentStatus.SUCCESS, startOfDay, endOfDay
        );

        BigDecimal weeklyRevenue = orderRepo.calculateRevenueBetween(
                id,
                PaymentStatus.SUCCESS,
                startOfWeekDateTime,
                endOfDay
        );

        BigDecimal monthlyRevenue = orderRepo.calculateRevenueBetween(
                id,
                PaymentStatus.SUCCESS,
                startOfMonthDateTime,
                endOfDay
        );

        Long successfulPayments =
                orderRepo.countByRestaurant_IdAndPaymentStatus(
                        id, PaymentStatus.SUCCESS
                );

        return new RevenueSummaryResponseDto(
                safeAmount(todayRevenue),
                safeAmount(weeklyRevenue),
                safeAmount(monthlyRevenue),
                successfulPayments
        );
    }

    @Override
    public OrderStatusSummaryResponseDto getOrderStatusSummary(Long restaurantId) {

        Restaurant restaurant = getAuthorizedRestaurant(restaurantId);

        Long id = restaurant.getId();

        return new OrderStatusSummaryResponseDto(

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.PLACED
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.CONFIRMED
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.PREPARING
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.READY
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.SERVED
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.COMPLETED
                ),

                orderRepo.countByRestaurant_IdAndOrderStatus(
                        id, OrderStatus.CANCELLED
                )
        );
    }


    @Override
    public List<DailyRevenueResponseDto> getDailyRevenue(
            Long restaurantId,
            LocalDate startDate,
            LocalDate endDate) {

        // 1. Validate restaurant ownership
        Restaurant restaurant = getAuthorizedRestaurant(restaurantId);

        // 2. Validate date range
        if (startDate == null || endDate == null) {
            throw new BadRequestException(
                    "Start date and end date are required"
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException(
                    "Start date cannot be after end date"
            );
        }

        // Limit the range to 31 days
        if (startDate.plusDays(30).isBefore(endDate)) {
            throw new BadRequestException(
                    "Date range cannot exceed 31 days"
            );
        }

        // 3. Fetch successful payments
        List<Payment> payments =
                paymentRepo
                        .findAllByCustomerOrder_Restaurant_IdAndPaymentStatusAndPaidAtGreaterThanEqualAndPaidAtLessThanOrderByPaidAtAsc(
                                restaurant.getId(),
                                PaymentStatus.SUCCESS,
                                startDate.atStartOfDay(),
                                endDate.plusDays(1).atStartOfDay()
                        );

        // 4. Group payments by payment date
        Map<LocalDate, BigDecimal> revenueMap = new HashMap<>();
        Map<LocalDate, Long> countMap = new HashMap<>();

        for (Payment payment : payments) {

            LocalDate paymentDate = payment.getPaidAt().toLocalDate();

            revenueMap.merge(
                    paymentDate,
                    payment.getAmount(),
                    BigDecimal::add
            );

            countMap.merge(paymentDate, 1L, Long::sum);
        }

        // 5. Include dates with zero revenue
        List<DailyRevenueResponseDto> result = new ArrayList<>();

        for (LocalDate date = startDate;
             !date.isAfter(endDate);
             date = date.plusDays(1)) {

            result.add(
                    new DailyRevenueResponseDto(
                            date,
                            revenueMap.getOrDefault(date, BigDecimal.ZERO),
                            countMap.getOrDefault(date, 0L)
                    )
            );
        }

        return result;
    }


    @Override
    public MenuAndTableSummaryResponseDto getMenuAndTableSummary(
            Long restaurantId) {

        // Validate restaurant ownership
        Restaurant restaurant = getAuthorizedRestaurant(restaurantId);

        Long id = restaurant.getId();

        // Category statistics
        Long totalCategories =
                menuCategoryRepo.countByRestaurant_Id(id);

        Long activeCategories =
                menuCategoryRepo.countByRestaurant_IdAndStatus(
                        id,
                        MenuCategoryStatus.ACTIVE
                );

        Long inactiveCategories =
                menuCategoryRepo.countByRestaurant_IdAndStatus(
                        id,
                        MenuCategoryStatus.INACTIVE
                );

        // Menu item statistics
        Long totalMenuItems =
                menuItemRepo.countByCategory_Restaurant_Id(id);

        Long availableItems =
                menuItemRepo.countByCategory_Restaurant_IdAndStatus(
                        id,
                        MenuItemStatus.AVAILABLE
                );

        Long unavailableItems =
                menuItemRepo.countByCategory_Restaurant_IdAndStatus(
                        id,
                        MenuItemStatus.UNAVAILABLE
                );

        // Dining table statistics
        Long totalTables =
                diningTableRepo.countByRestaurant_Id(id);

        Long availableTables =
                diningTableRepo.countByRestaurant_IdAndStatus(
                        id,
                        DiningTableStatus.AVAILABLE
                );

        Long occupiedTables =
                diningTableRepo.countByRestaurant_IdAndStatus(
                        id,
                        DiningTableStatus.OCCUPIED
                );

        Long inactiveTables =
                diningTableRepo.countByRestaurant_IdAndStatus(
                        id,
                        DiningTableStatus.INACTIVE
                );

        return new MenuAndTableSummaryResponseDto(
                totalCategories,
                activeCategories,
                inactiveCategories,
                totalMenuItems,
                availableItems,
                unavailableItems,
                totalTables,
                availableTables,
                occupiedTables,
                inactiveTables
        );
    }




    @Override
    public List<PaymentMethodSummaryResponseDto> getPaymentMethodSummary(
            Long restaurantId) {

        Restaurant restaurant = getAuthorizedRestaurant(restaurantId);

        Long id = restaurant.getId();

        return java.util.Arrays.stream(PaymentMethod.values())
                .map(method -> {

                    Long totalPayments = paymentRepo
                            .countByCustomerOrder_Restaurant_IdAndPaymentMethodAndPaymentStatus(
                                    id,
                                    method,
                                    PaymentStatus.SUCCESS
                            );

                    BigDecimal totalAmount = paymentRepo
                            .calculateAmountByPaymentMethod(
                                    id,
                                    method,
                                    PaymentStatus.SUCCESS
                            );

                    return new PaymentMethodSummaryResponseDto(
                            method,
                            totalPayments,
                            safeAmount(totalAmount)
                    );
                })
                .toList();
    }


    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }


    @Override
    public OrderPageResponse<OrderResponseDto> filterOrders(
            Long restaurantId,
            OrderFilterRequestDto request,
            int page,
            int size) {

        Restaurant restaurant = getAuthorizedRestaurant(restaurantId);

        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("Invalid pagination parameters");
        }

        if (request.startDate() != null &&
                request.endDate() != null &&
                request.startDate().isAfter(request.endDate())) {

            throw new BadRequestException(
                    "Start date cannot be after end date"
            );
        }

        LocalDate startDate = request.startDate() != null
                ? request.startDate()
                : LocalDate.of(2000, 1, 1);

        LocalDate endDate = request.endDate() != null
                ? request.endDate()
                : LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(page, size);

        Page<CustomerOrder> orders;

        if (request.orderStatus() != null) {

            orders = orderRepo
                    .findAllByRestaurant_IdAndOrderStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                            restaurant.getId(),
                            request.orderStatus(),
                            start,
                            end,
                            pageable
                    );

        } else {

            orders = orderRepo
                    .findAllByRestaurant_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                            restaurant.getId(),
                            start,
                            end,
                            pageable
                    );
        }

        List<OrderResponseDto> content = orders.getContent()
                .stream()
                .map(this::mapToOrderResponse)
                .toList();

        return new OrderPageResponse<>(
                content,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
    }

    private OrderResponseDto mapToOrderResponse(CustomerOrder order) {
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

}
