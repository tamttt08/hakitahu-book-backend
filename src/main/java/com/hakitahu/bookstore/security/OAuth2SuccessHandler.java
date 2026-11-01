package com.hakitahu.bookstore.security;

import com.hakitahu.bookstore.entity.User;
import com.hakitahu.bookstore.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Trích xuất dữ liệu từ Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getName(); // Hoặc dùng oAuth2User.getAttribute("sub")

        // 1. Kiểm tra và hợp nhất tài khoản
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            // Nếu email đã tồn tại (do từng đăng ký Local), cập nhật thêm Provider Google
            user.setProvider("GOOGLE");
            user.setProviderId(googleId);
            user.setIsEmailVerified(true);
            userRepository.save(user);
        } else {
            // Nếu là email hoàn toàn mới, khởi tạo tài khoản
            user = new User();
            user.setEmail(email);
            user.setUsername(email); // Dùng email làm tên đăng nhập mặc định
            user.setFullName(name != null ? name : "Google User");
            user.setProvider("GOOGLE");
            user.setProviderId(googleId);
            user.setIsEmailVerified(true);
            user.setRole("USER");
            userRepository.save(user);
        }

        // 2. Sinh JWT token cho hệ thống nội bộ
        String jwt = jwtUtils.generateToken(user.getUsername());

        // 3. Chuyển hướng kèm token về giao diện Vue 3
        String targetUrl = "http://localhost:5173/oauth2/redirect?token=" + jwt;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}