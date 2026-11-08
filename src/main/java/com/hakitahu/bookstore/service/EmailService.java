package com.hakitahu.bookstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;


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
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
        	MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Yêu cầu khôi phục mật khẩu - HAKITAHU BOOK");

            // Tạo đường link trỏ về giao diện ResetPassword của Vue 3
            String resetLink = "http://localhost:5173/reset-password?token=" + token;
            
            // Thiết kế template HTML cho email chuyên nghiệp
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; border-radius: 10px;'>"
                    + "<div style='background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
                    + "<h2 style='color: #333;'>Khôi phục mật khẩu</h2>"
                    + "<p>Xin chào,</p>"
                    + "<p>Hệ thống nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại HAKITAHU BOOK.</p>"
                    + "<p>Vui lòng click vào nút bên dưới để tạo mật khẩu mới. Link này chỉ có hiệu lực trong <b>15 phút</b>.</p>"
                    + "<div style='text-align: center; margin: 30px 0;'>"
                    + "<a href='" + resetLink + "' style='display: inline-block; padding: 12px 24px; background-color: #308ce8; color: white; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 16px;'>Đặt lại mật khẩu ngay</a>"
                    + "</div>"
                    + "<p style='color: #666; font-size: 14px;'>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email và bảo mật tài khoản của mình.</p>"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true); // true để bật chế độ biên dịch HTML
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email khôi phục mật khẩu: " + e.getMessage());
        }
    }
}