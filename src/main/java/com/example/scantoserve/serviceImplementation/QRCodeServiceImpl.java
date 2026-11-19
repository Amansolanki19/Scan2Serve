package com.example.scantoserve.serviceImplementation;


import com.example.scantoserve.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRCodeServiceImpl implements QRCodeService {

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public byte[] generateQRCode(String qrToken) {

        String baseUrl = frontendBaseUrl.replaceAll("/+$", "");

        String qrContent = baseUrl + "/menu/" + qrToken;

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    bitMatrix,
                    "PNG",
                    outputStream
            );

            return outputStream.toByteArray();

        } catch (WriterException | IOException exception) {
            throw new IllegalStateException(
                    "Unable to generate QR code",
                    exception
            );
        }
    }
}