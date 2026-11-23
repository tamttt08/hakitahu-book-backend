package com.hakitahu.bookstore.controller;

import com.hakitahu.bookstore.service.EmailService;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hakitahu.bookstore.dto.request.LoginRequestDTO;
import com.hakitahu.bookstore.dto.request.RegisterRequestDTO;
import com.hakitahu.bookstore.dto.response.AuthResponseDTO;
import com.hakitahu.bookstore.dto.response.UserDTO;
import com.hakitahu.bookstore.entity.User;
import com.hakitahu.bookstore.entity.VerificationToken;
import com.hakitahu.bookstore.repository.UserRepository;
import com.hakitahu.bookstore.service.AuthService;
import java.util.UUID;
import com.hakitahu.bookstore.repository.VerificationTokenRepository;

@CrossOrigin(origins = "*") // Cho phép Vue 3 (localhost:5173) gọi API không bị chặn
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmailService emailService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public AuthController(EmailService emailService,
                          AuthService authService,
                          UserRepository userRepository,
                          VerificationTokenRepository verificationTokenRepository) {
        this.emailService = emailService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO request) {
        try {
            String message = authService.registerUser(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            String message = authService.verifyEmail(token);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            AuthResponseDTO response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            // Lấy tên đăng nhập từ token JWT đang gửi lên
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            UserDTO userDto = new UserDTO();
            userDto.setUserId(user.getUserId());
            userDto.setUsername(user.getUsername());
            userDto.setFullName(user.getFullName());
            userDto.setEmail(user.getEmail());
            userDto.setPhone(user.getPhone());
            userDto.setAddress(user.getAddress());
            userDto.setRole(user.getRole());
            userDto.setProvider(user.getProvider());
            userDto.setIsEmailVerified(user.getIsEmailVerified());

            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok(authService.forgotPassword(request.get("email")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            return ResponseEntity.ok(authService.resetPassword(request.get("token"), request.get("newPassword")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody Map<String, String> request) {
        try {
            // Cần truyền token JWT lên Header để Spring Security nhận diện được
            // authentication
            String username = authentication.getName();
            return ResponseEntity
                    .ok(authService.changePassword(username, request.get("oldPassword"), request.get("newPassword")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập địa chỉ email!");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy tài khoản nào đăng ký với email này!");
        }

        if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
            return ResponseEntity.badRequest()
                    .body("Tài khoản này đã được xác thực trước đó. Bạn có thể đăng nhập ngay!");
        }

        // Tạo mã token ngẫu nhiên mới
        String newToken = UUID.randomUUID().toString();

        // Nếu user đã có token cũ trong bảng VerificationTokens thì cập nhật lại (tránh
        // lỗi trùng khóa ngoại / rác DB)
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user)
                .orElse(new VerificationToken());
        verificationToken.setUser(user);
        verificationToken.setToken(newToken);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // Sửa thành Date nếu entity cậu dùng
                                                                              // java.util.Date

        verificationTokenRepository.save(verificationToken);

        try {
            // Gọi lại đúng hàm gửi mail cậu đã viết ở luồng register
            emailService.sendVerificationEmail(user.getEmail(), newToken);
            return ResponseEntity.ok("Đã gửi lại email xác nhận! Vui lòng kiểm tra hộp thư (kể cả mục Spam).");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Gửi email thất bại do lỗi kết nối máy chủ thư. Vui lòng thử lại sau ít giây!");
        }
    }
}