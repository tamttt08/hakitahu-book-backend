package com.hakitahu.bookstore.controller;

import com.hakitahu.bookstore.entity.User;
import com.hakitahu.bookstore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Lấy toàn bộ danh sách người dùng
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // 2. Admin thêm người dùng mới
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại!");
        }
        if (newUser.getEmail() != null && userRepository.existsByEmail(newUser.getEmail())) {
            return ResponseEntity.badRequest().body("Email này đã được sử dụng!");
        }

        // Mã hóa mật khẩu bằng BCrypt
        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        } else {
            newUser.setPassword(passwordEncoder.encode("123456")); // Mật khẩu mặc định nếu bỏ trống
        }

        if (newUser.getRole() == null || newUser.getRole().isEmpty()) {
            newUser.setRole("user");
        }
        if (newUser.getStatus() == null) {
            newUser.setStatus(true);
        }
        newUser.setIsEmailVerified(true);
        newUser.setProvider("LOCAL");

        User savedUser = userRepository.save(newUser);
        return ResponseEntity.ok(savedUser);
    }

    // 3. Admin cập nhật thông tin, Vai trò (Role) hoặc Khóa/Mở khóa (Status)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        if (userDetails.getStatus() != null) {
            user.setStatus(userDetails.getStatus());
        }

        // Nếu Admin có nhập mật khẩu mới để cấp lại cho user
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword().trim()));
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    // 4. Xóa người dùng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}