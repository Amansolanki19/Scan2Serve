
package com.example.scantoserve.controller;

import com.example.scantoserve.service.InvoiceService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/orders/{orderId}/download")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long orderId) {

        byte[] pdf = invoiceService.generateInvoice(orderId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ScanToServe_Invoice_" +
                                orderId + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
