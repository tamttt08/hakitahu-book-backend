package com.hakitahu.bookstore.controller;

import com.hakitahu.bookstore.dto.profile.ChangePasswordDTO;
import com.hakitahu.bookstore.dto.profile.UpdateProfileDTO;
import com.hakitahu.bookstore.entity.User;
import com.hakitahu.bookstore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Lấy thông tin hồ sơ người dùng đang đăng nhập
    @GetMapping
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) {
            return ResponseEntity.status(401).body("Không tìm thấy phiên đăng nhập hợp lệ!");
        }
        return ResponseEntity.ok(buildUserResponse(user));
    }

    // 2. Cập nhật thông tin cá nhân (Họ tên, Email, SĐT, Địa chỉ)
    @PutMapping
    public ResponseEntity<?> updateMyProfile(Authentication authentication, @RequestBody UpdateProfileDTO dto) {
        User user = getCurrentUser(authentication);
        if (user == null) {
            return ResponseEntity.status(401).body("Không tìm thấy phiên đăng nhập hợp lệ!");
        }

        // Cập nhật Họ & tên
        if (dto.getFullName() != null) {
            String fullName = dto.getFullName().trim();
            if (fullName.isEmpty()) {
                return ResponseEntity.badRequest().body("Họ và tên không được để trống!");
            }
            user.setFullName(fullName);
        }

        // Cập nhật Email (kiểm tra trùng với tài khoản khác)
        if (dto.getEmail() != null) {
            String newEmail = dto.getEmail().trim();
            if (newEmail.isEmpty() || !newEmail.contains("@")) {
                return ResponseEntity.badRequest().body("Địa chỉ email không hợp lệ!");
            }
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                Optional<User> existing = userRepository.findByEmail(newEmail);
                if (existing.isPresent() && !existing.get().getUserId().equals(user.getUserId())) {
                    return ResponseEntity.badRequest().body("Email này đã được sử dụng bởi tài khoản khác!");
                }
                user.setEmail(newEmail);
            }
        }

        // Cập nhật Số điện thoại
        if (dto.getPhone() != null) {
            String phone = dto.getPhone().trim();
            if (!phone.isEmpty() && !phone.matches("^(0|\\+84)[0-9]{9,10}$")) {
                return ResponseEntity.badRequest().body("Số điện thoại không đúng định dạng Việt Nam (10-11 số)!");
            }
            user.setPhone(phone);
        }

        // Cập nhật Địa chỉ giao hàng
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress().trim());
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(buildUserResponse(saved));
    }

    // 3. Đổi mật khẩu
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePasswordDTO dto) {
        User user = getCurrentUser(authentication);
        if (user == null) {
            return ResponseEntity.status(401).body("Không tìm thấy phiên đăng nhập hợp lệ!");
        }

        if ("GOOGLE".equalsIgnoreCase(user.getProvider())) {
            return ResponseEntity.badRequest().body("Tài khoản đăng nhập bằng Google không hỗ trợ đổi mật khẩu tại đây!");
        }

        if (dto.getCurrentPassword() == null || dto.getNewPassword() == null) {
            return ResponseEntity.badRequest().body("Vui lòng nhập đầy đủ mật khẩu hiện tại và mật khẩu mới!");
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không chính xác!");
        }

        if (dto.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu mới không được trùng với mật khẩu cũ!");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String principal = authentication.getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElse(null);
    }

    // Trả về dữ liệu sạch (không lộ mật khẩu) để Frontend cập nhật lại localStorage
    private Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("address", user.getAddress());
        data.put("role", user.getRole());
        data.put("provider", user.getProvider());
        return data;
    }
}