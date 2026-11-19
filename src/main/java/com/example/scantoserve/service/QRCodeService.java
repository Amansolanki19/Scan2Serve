package com.example.scantoserve.service;


public interface QRCodeService {

    byte[] generateQRCode(String qrToken);
}
