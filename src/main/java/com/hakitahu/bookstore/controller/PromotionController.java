package com.hakitahu.bookstore.controller;

import com.hakitahu.bookstore.dto.promotion.PromotionDTO;
import com.hakitahu.bookstore.entity.Book;
import com.hakitahu.bookstore.entity.Promotion;
import com.hakitahu.bookstore.repository.BookRepository;
import com.hakitahu.bookstore.repository.PromotionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "http://localhost:5173")
public class PromotionController {

    private final PromotionRepository promotionRepository;
    private final BookRepository bookRepository;

    public PromotionController(PromotionRepository promotionRepository, BookRepository bookRepository) {
        this.promotionRepository = promotionRepository;
        this.bookRepository = bookRepository;
    }

    // 1. Lấy tất cả chương trình giảm giá (Admin)
    @GetMapping
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        return ResponseEntity.ok(promotionRepository.findAllByOrderByPromotionIdDesc());
    }

    // 2. Lấy các chương trình đang chạy (Public - cho trang /promotions ngoài cửa hàng)
    @GetMapping("/active")
    public ResponseEntity<List<Promotion>> getActivePromotions() {
        return ResponseEntity.ok(promotionRepository.findActivePromotions(LocalDateTime.now()));
    }

    // 3. Tạo chương trình giảm giá mới & gán danh sách sách áp dụng
    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody PromotionDTO request) {
        String error = validateRequest(request);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        Promotion promotion = new Promotion();
        mapRequestToEntity(request, promotion);
        return ResponseEntity.ok(promotionRepository.save(promotion));
    }

    // 4. Cập nhật chương trình giảm giá & thay đổi danh sách sách áp dụng
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable Integer id, @RequestBody PromotionDTO request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình khuyến mãi với ID: " + id));

        String error = validateRequest(request);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        mapRequestToEntity(request, promotion);
        return ResponseEntity.ok(promotionRepository.save(promotion));
    }

    // 5. Xóa chương trình giảm giá
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình khuyến mãi với ID: " + id));
        promotionRepository.delete(promotion);
        return ResponseEntity.noContent().build();
    }

    private void mapRequestToEntity(PromotionDTO request, Promotion promotion) {
        promotion.setName(request.getName().trim());
        promotion.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus(request.getStatus() != null ? request.getStatus() : true);

        Set<Book> selectedBooks = new HashSet<>(bookRepository.findAllById(request.getBookIds()));
        promotion.setBooks(selectedBooks);
    }

    private String validateRequest(PromotionDTO req) {
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            return "Tên chương trình khuyến mãi không được để trống!";
        }
        if (!"PERCENT".equals(req.getDiscountType()) && !"FIXED".equals(req.getDiscountType())) {
            return "Loại giảm giá phải là PERCENT hoặc FIXED!";
        }
        if (req.getDiscountValue() == null || req.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            return "Mức giảm phải lớn hơn 0!";
        }
        if ("PERCENT".equals(req.getDiscountType()) && req.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            return "Phần trăm giảm giá không được vượt quá 100%!";
        }
        if (req.getStartDate() == null || req.getEndDate() == null) {
            return "Vui lòng chọn đầy đủ thời gian bắt đầu và kết thúc!";
        }
        if (!req.getEndDate().isAfter(req.getStartDate())) {
            return "Thời gian kết thúc phải sau thời gian bắt đầu!";
        }
        if (req.getBookIds() == null || req.getBookIds().isEmpty()) {
            return "Vui lòng chọn ít nhất 1 cuốn sách để áp dụng giảm giá!";
        }
        return null;
    }
}