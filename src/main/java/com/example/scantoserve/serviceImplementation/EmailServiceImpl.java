package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderMail;

    @Override
    public void sendOtpEmail(String email, String otp, long expirationMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderMail);
        message.setTo(email);
        message.setSubject("ScanToServe Otp Verification code");
        message.setText(
                "Your ScanToServe login OTP is: " + otp +
                        "\n\nThis OTP expires in " + expirationMinutes + " minutes." +
                        "\nDo not share this OTP with anyone."
        );

        javaMailSender.send(message);
    }

    @Value("${app.mail.from}")
    private String fromEmail;

    @Override
    public void sendInvoiceEmail(String toEmail, byte[] invoicePdf, Long orderId) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            helper.setSubject(
                    "Your ScanToServe Invoice - Order #" + orderId
            );

            helper.setText(
                    """
                    Hello,
    
                    Thank you for dining with us!
    
                    Your order has been successfully processed.
                    Please find your invoice attached to this email.
    
                    Order ID: %d
    
                    Thank you for choosing ScanToServe.
    
                    Regards,
                    ScanToServe Team
                    """.formatted(orderId)
            );

            helper.addAttachment(
                    "ScanToServe_Invoice_" + orderId + ".pdf",
                    () -> new ByteArrayInputStream(invoicePdf),
                    "application/pdf"
            );

            javaMailSender.send(message);

        } catch (MessagingException exception) {

            throw new RuntimeException(
                    "Unable to send invoice email",
                    exception
            );
        }
    }

}
