
package com.example.scantoserve.service;

public interface InvoiceService {

    byte[] generateInvoice(Long orderId);
}
