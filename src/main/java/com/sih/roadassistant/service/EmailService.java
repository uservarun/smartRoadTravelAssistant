package com.sih.roadassistant.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String senderEmail;

    @Async
    public void sendVerificationEmail(String recipientEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage(); // Use MimeMessage type
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(senderEmail, "Road Assistant");
            helper.setTo(recipientEmail);
            helper.setSubject("Road Assistant - Verify Your Email Address");
            
            String htmlContent = "<html>"
                    + "<body style='font-family: Arial, sans-serif; background-color: #f4f7f6; padding: 20px;'>"
                    + "<div style='max-width: 600px; margin: auto; background: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border-top: 5px solid #2F3E46;'>"
                    + "<h2 style='color: #2F3E46; text-align: center; margin-bottom: 20px;'>Welcome to RoadSathi!</h2>"
                    + "<p style='font-size: 16px; color: #354F52; line-height: 1.5;'>Thank you for signing up for RoadSathi. To activate your account and start planning safer trips, please verify your email address using this verification code:</p>"
                    + "<div style='background-color: #CAD2C5; padding: 15px; border-radius: 6px; text-align: center; margin: 30px 0;'>"
                    + "<span style='font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #2F3E46;'>" + code + "</span>"
                    + "</div>"
                    + "<p style='font-size: 13px; color: #84A98C; text-align: center;'>This verification code is temporary and will expire in 15 minutes.</p>"
                    + "<hr style='border: none; border-top: 1px solid #ECEEEF; margin: 30px 0;'>"
                    + "<p style='font-size: 11px; color: #999; text-align: center;'>If you did not request this registration, you can safely ignore this email.</p>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("Email successfully sent to: " + recipientEmail);
        } catch (Exception e) {
            System.err.println("Failed to send SMTP email: " + e.getMessage());
        }
    }
}
