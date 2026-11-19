
package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.service.InvoiceEmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceEmailController {

    private final InvoiceEmailService invoiceEmailService;

    @PostMapping("/orders/{orderId}/send-email")
    public ResponseEntity<ApiResponse<Void>> sendInvoiceEmail(
            @PathVariable Long orderId) {

        invoiceEmailService.sendInvoiceToCustomer(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Invoice sent successfully to customer's email",
                        null
                )
        );
    }
}
