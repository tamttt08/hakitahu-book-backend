package com.hakitahu.bookstore.service;

import com.hakitahu.bookstore.dto.request.LoginRequestDTO;
import com.hakitahu.bookstore.dto.request.RegisterRequestDTO;
import com.hakitahu.bookstore.dto.response.AuthResponseDTO;
import com.hakitahu.bookstore.dto.response.UserDTO;
import com.hakitahu.bookstore.entity.User;
import com.hakitahu.bookstore.entity.VerificationToken;
import com.hakitahu.bookstore.repository.UserRepository;
import com.hakitahu.bookstore.repository.VerificationTokenRepository;
import com.hakitahu.bookstore.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    public String registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // 1. Lưu User mới với trạng thái chưa xác thực
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setIsEmailVerified(false);
        user.setProvider("LOCAL");
        user.setRole("USER");
        userRepository.save(user);

        // 2. Tạo mã UUID ngẫu nhiên để làm Token xác thực
        String tokenString = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(tokenString);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        // 3. Gửi Email
        emailService.sendVerificationEmail(user.getEmail(), tokenString);

        return "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản.";
    }

    public String verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc không tồn tại!"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link xác thực đã hết hạn!");
        }

        User user = verificationToken.getUser();
        user.setIsEmailVerified(true);
        userRepository.save(user);
        
        tokenRepository.delete(verificationToken); // Xóa token sau khi dùng xong

        return "Xác thực email thành công! Bạn có thể đăng nhập.";
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Sai tên đăng nhập hoặc mật khẩu!"));

        // Chặn đăng nhập nếu chưa xác thực Email
        if (!user.getIsEmailVerified()) {
            throw new RuntimeException("Vui lòng truy cập email để xác thực tài khoản trước khi đăng nhập!");
        }

        // Ủy quyền cho Spring Security kiểm tra khớp mật khẩu
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String jwt = jwtUtils.generateToken(authentication.getName());

        UserDTO userDto = new UserDTO();
        userDto.setUserId(user.getUserId());
        userDto.setUsername(user.getUsername());
        userDto.setFullName(user.getFullName());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        userDto.setProvider(user.getProvider());
        userDto.setIsEmailVerified(user.getIsEmailVerified());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(jwt);
        response.setUser(userDto);

        return response;
    }
    
 // 1. Gửi link quên mật khẩu
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

        if ("GOOGLE".equals(user.getProvider()) && user.getPassword() == null) {
            throw new RuntimeException("Tài khoản này đăng nhập bằng Google, không thể đổi mật khẩu nội bộ!");
        }

        // Tạo token mới
        String tokenString = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setToken(tokenString);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // Link reset chỉ sống 15 phút
        tokenRepository.save(token);

        // Gửi email (Cậu nhớ tạo thêm hàm sendPasswordResetEmail trong EmailService nhé)
        emailService.sendPasswordResetEmail(user.getEmail(), tokenString);
        return "Link khôi phục mật khẩu đã được gửi vào email của bạn.";
    }

    // 2. Đặt lại mật khẩu mới (từ link email)
    public String resetPassword(String token, String newPassword) {
        VerificationToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link không hợp lệ hoặc đã hết hạn!"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Link khôi phục đã hết hạn!");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        tokenRepository.delete(resetToken); // Dùng xong xóa luôn
        return "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập.";
    }

    // 3. Đổi mật khẩu (Khi đã đăng nhập)
    public String changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực người dùng!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Đổi mật khẩu thành công!";
    }
}