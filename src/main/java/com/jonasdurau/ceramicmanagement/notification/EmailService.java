package com.jonasdurau.ceramicmanagement.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, @Value("${FRONTEND_BASE_URL}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Redefinição de Senha");
        message.setFrom("gestorceramico@gmail.com");
        String resetUrl = frontendBaseUrl + "/reset-password/" + token;
        message.setText("Para redefinir sua senha, clique no link: " + resetUrl);
        mailSender.send(message);
    }
}
