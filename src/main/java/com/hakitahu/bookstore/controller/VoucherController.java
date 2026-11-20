package com.hakitahu.bookstore.controller;

import com.hakitahu.bookstore.entity.Voucher;
import com.hakitahu.bookstore.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "http://localhost:5173")
public class VoucherController {

    @Autowired
    private VoucherRepository voucherRepository;

    // 1. Lấy tất cả voucher (Admin)
    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherRepository.findAllByOrderByVoucherIdDesc());
    }

    // 2. Lấy các voucher đang khả dụng (Public - cho trang Khuyến mãi & Giỏ hàng)
    @GetMapping("/active")
    public ResponseEntity<List<Voucher>> getActiveVouchers() {
        return ResponseEntity.ok(voucherRepository.findActiveVouchers(LocalDateTime.now()));
    }

    // 3. Admin tạo mới voucher
    @PostMapping
    public ResponseEntity<?> createVoucher(@RequestBody Voucher voucher) {
        String validationError = validateVoucherData(voucher);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        String normalizedCode = voucher.getCode().trim().toUpperCase();
        if (voucherRepository.existsByCodeIgnoreCase(normalizedCode)) {
            return ResponseEntity.badRequest().body("Mã khuyến mãi \"" + normalizedCode + "\" đã tồn tại!");
        }

        voucher.setCode(normalizedCode);
        voucher.setUsedCount(0);
        if ("FIXED".equals(voucher.getDiscountType())) {
            voucher.setMaxDiscount(null);
        }

        return ResponseEntity.ok(voucherRepository.save(voucher));
    }

    // 4. Admin cập nhật voucher
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVoucher(@PathVariable Integer id, @RequestBody Voucher details) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã khuyến mãi với ID: " + id));

        String validationError = validateVoucherData(details);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        String normalizedCode = details.getCode().trim().toUpperCase();
        if (!voucher.getCode().equalsIgnoreCase(normalizedCode)
                && voucherRepository.existsByCodeIgnoreCase(normalizedCode)) {
            return ResponseEntity.badRequest().body("Mã khuyến mãi \"" + normalizedCode + "\" đã được sử dụng!");
        }

        if (details.getQuantity() < voucher.getUsedCount()) {
            return ResponseEntity.badRequest()
                    .body("Tổng số lượt dùng không thể nhỏ hơn số lượt đã sử dụng (" + voucher.getUsedCount() + ")!");
        }

        voucher.setCode(normalizedCode);
        voucher.setDiscountType(details.getDiscountType());
        voucher.setDiscountValue(details.getDiscountValue());
        voucher.setMinOrderValue(details.getMinOrderValue() != null ? details.getMinOrderValue() : BigDecimal.ZERO);
        voucher.setMaxDiscount("PERCENT".equals(details.getDiscountType()) ? details.getMaxDiscount() : null);
        voucher.setQuantity(details.getQuantity());
        voucher.setStartDate(details.getStartDate());
        voucher.setEndDate(details.getEndDate());
        voucher.setStatus(details.getStatus() != null ? details.getStatus() : true);

        return ResponseEntity.ok(voucherRepository.save(voucher));
    }

    // 5. Admin xóa voucher
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVoucher(@PathVariable Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã khuyến mãi với ID: " + id));
        try {
            voucherRepository.delete(voucher);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body("Không thể xóa mã đã có khách hàng sử dụng. Hãy chuyển trạng thái sang Ngưng hoạt động!");
        }
    }

    private String validateVoucherData(Voucher v) {
        if (v.getCode() == null || v.getCode().trim().isEmpty()) {
            return "Mã voucher không được để trống!";
        }
        if (v.getDiscountType() == null || (!"PERCENT".equals(v.getDiscountType()) && !"FIXED".equals(v.getDiscountType()))) {
            return "Loại giảm giá phải là PERCENT hoặc FIXED!";
        }
        if (v.getDiscountValue() == null || v.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            return "Giá trị giảm phải lớn hơn 0!";
        }
        if ("PERCENT".equals(v.getDiscountType()) && v.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            return "Phần trăm giảm giá không được vượt quá 100%!";
        }
        if (v.getQuantity() == null || v.getQuantity() <= 0) {
            return "Số lượt sử dụng phải lớn hơn 0!";
        }
        if (v.getStartDate() == null || v.getEndDate() == null) {
            return "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!";
        }
        if (!v.getEndDate().isAfter(v.getStartDate())) {
            return "Ngày kết thúc phải sau ngày bắt đầu!";
        }
        return null;
    }
}