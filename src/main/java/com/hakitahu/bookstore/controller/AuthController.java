package com.hakitahu.bookstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.hakitahu.bookstore.repository.UserRepository;
import com.hakitahu.bookstore.service.AuthService;

@CrossOrigin(origins = "*") // Cho phép Vue 3 (localhost:5173) gọi API không bị chặn
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;

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
}