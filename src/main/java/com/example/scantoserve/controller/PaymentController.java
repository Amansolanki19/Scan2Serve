
package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.dto.*;
import com.example.scantoserve.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Record a customer's offline payment
    @PostMapping("/restaurants/{restaurantId}/orders/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> recordPayment(
            @PathVariable Long restaurantId,
            @PathVariable Long orderId,
            @Valid @RequestBody RecordPaymentRequestDto request) {

        PaymentResponseDto response = paymentService.recordPayment(
                restaurantId,
                orderId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment recorded successfully",
                        response
                )
        );
    }

    // Get payment details for a particular order
    @GetMapping("/restaurants/{restaurantId}/orders/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPayment(
            @PathVariable Long restaurantId,
            @PathVariable Long orderId) {

        PaymentResponseDto response = paymentService.getPaymentByOrderId(
                restaurantId,
                orderId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment details fetched successfully",
                        response
                )
        );
    }
}
