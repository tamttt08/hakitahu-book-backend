package com.hakitahu.bookstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:5173") // Thêm dòng này
public class FileUploadController {

    private final Path uploadDir = Paths.get("uploads");

    public FileUploadController() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục lưu ảnh!", e);
        }
    }

    @PostMapping
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File không được để trống!");
            }

            // Tạo tên file duy nhất tránh trùng lặp
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // Lưu file vào thư mục uploads/
            Path targetLocation = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL ngắn gọn (dưới 255 ký tự, vừa khít cột NVARCHAR(255) trong DB)
            String fileUrl = "http://localhost:8081/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (IOException ex) {
            return ResponseEntity.internalServerError().body("Lỗi khi tải ảnh lên: " + ex.getMessage());
        }
    }
}