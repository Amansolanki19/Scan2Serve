
package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.entity.CustomerOrder;
import com.example.scantoserve.entity.PaymentStatus;
import com.example.scantoserve.exception.BadRequestException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.CustomerOrderRepo;
import com.example.scantoserve.service.EmailService;
import com.example.scantoserve.service.InvoiceEmailService;
import com.example.scantoserve.service.InvoiceService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceEmailServiceImpl implements InvoiceEmailService {

    private final CustomerOrderRepo customerOrderRepo;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public void sendInvoiceToCustomer(Long orderId) {

        CustomerOrder order = customerOrderRepo.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (order.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException(
                    "Invoice email can only be sent after successful payment"
            );
        }

        String customerEmail = order.getCustomer().getEmail();

        if (customerEmail == null || customerEmail.isBlank()) {
            throw new BadRequestException(
                    "Customer email address is not available"
            );
        }

        byte[] invoicePdf = invoiceService.generateInvoice(orderId);

        emailService.sendInvoiceEmail(
                customerEmail,
                invoicePdf,
                orderId
        );
    }
}
