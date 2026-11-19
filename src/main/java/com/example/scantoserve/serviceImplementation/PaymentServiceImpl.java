
package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.*;
import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.BadRequestException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.CustomerOrderRepo;
import com.example.scantoserve.repository.PaymentRepo;
import com.example.scantoserve.repository.RestaurantRepo;
import com.example.scantoserve.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepo paymentRepo;
    private final CustomerOrderRepo orderRepo;
    private final RestaurantRepo restaurantRepo;

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        // Your existing UserDetails implementation uses email as username.
        return restaurantRepo.findByOwner_Email(userDetails.getUsername())
                .map(Restaurant::getOwner)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User account not found"));
    }

    private Restaurant getAuthorizedRestaurant(Long restaurantId, User user) {

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return restaurantRepo.findById(restaurantId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Restaurant not found"));
        }

        if (user.getRole() != UserRole.RESTAURANT_ADMIN) {
            throw new AccessDeniedException(
                    "Only restaurant administrators can manage payments"
            );
        }

        return restaurantRepo.findByIdAndOwner_Id(restaurantId, user.getId())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You do not have access to this restaurant"
                        ));
    }

    @Override
    public PaymentResponseDto recordPayment(Long restaurantId, Long orderId, RecordPaymentRequestDto request) {

        User currentUser = getCurrentUser();

        Restaurant restaurant =
                getAuthorizedRestaurant(restaurantId, currentUser);

        CustomerOrder order = orderRepo
                .findByIdAndRestaurant_Id(orderId, restaurant.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Payment cannot be recorded for a cancelled order"
            );
        }

        Payment payment = paymentRepo
                .findByCustomerOrder_Id(orderId)
                .orElseGet(() -> Payment.builder()
                        .customerOrder(order)
                        .amount(order.getTotalAmount())
                        .build());

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException(
                    "Payment has already been recorded for this order"
            );
        }

        if (request.paymentMethod() == PaymentMethod.UPI &&
                (request.transactionReference() == null ||
                        request.transactionReference().isBlank())) {
            throw new BadRequestException(
                    "Transaction reference is required for UPI payments"
            );
        }

        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setAmount(order.getTotalAmount());
        payment.setTransactionReference(
                request.transactionReference()
        );
        payment.setRecordedBy(currentUser);
        payment.setPaidAt(LocalDateTime.now());

        Payment savedPayment = paymentRepo.save(payment);

        // Keep the order's payment status synchronized.
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        orderRepo.save(order);

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(
            Long restaurantId,
            Long orderId) {

        User currentUser = getCurrentUser();

        Restaurant restaurant =
                getAuthorizedRestaurant(restaurantId, currentUser);

        orderRepo.findByIdAndRestaurant_Id(orderId, restaurant.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        Payment payment = paymentRepo
                .findByCustomerOrder_Id(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment record not found"));

        return mapToResponse(payment);
    }

    private PaymentResponseDto mapToResponse(Payment payment) {

        User recordedBy = payment.getRecordedBy();

        return new PaymentResponseDto(
                payment.getId(),
                payment.getCustomerOrder().getId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getTransactionReference(),
                recordedBy != null ? recordedBy.getId() : null,
                recordedBy != null ? recordedBy.getFullName() : null,
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
