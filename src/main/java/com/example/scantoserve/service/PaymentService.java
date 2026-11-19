
package com.example.scantoserve.service;

import com.example.scantoserve.dto.*;

public interface PaymentService {

    PaymentResponseDto recordPayment(
            Long restaurantId,
            Long orderId,
            RecordPaymentRequestDto request
    );

    PaymentResponseDto getPaymentByOrderId(
            Long restaurantId,
            Long orderId
    );
}
