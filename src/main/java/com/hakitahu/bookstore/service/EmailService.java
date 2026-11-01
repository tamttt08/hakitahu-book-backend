package com.hakitahu.bookstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Xác thực tài khoản HAKITAHU BOOK";
        String verificationUrl = "http://localhost:5173/verify?token=" + token; 
        
        String message = "Chào bạn,\n\n"
                + "Cảm ơn bạn đã đăng ký tài khoản tại HAKITAHU BOOK. Vui lòng click vào đường link bên dưới để xác thực email và kích hoạt tài khoản:\n\n"
                + verificationUrl + "\n\n"
                + "Lưu ý: Link này sẽ tự động hết hạn sau 24 giờ.\n\n"
                + "Trân trọng,\nĐội ngũ HAKITAHU BOOK";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);
    }
}