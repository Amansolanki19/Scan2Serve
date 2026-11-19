package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}
