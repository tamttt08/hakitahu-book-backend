package com.hakitahu.bookstore.repository;

import com.hakitahu.bookstore.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Voucher> findAllByOrderByVoucherIdDesc();

    // Dùng cho trang Khuyến mãi ngoài cửa hàng (lấy voucher còn hiệu lực và còn lượt)
    @Query("SELECT v FROM Voucher v WHERE v.status = true AND v.startDate <= :now AND v.endDate >= :now AND v.usedCount < v.quantity ORDER BY v.endDate ASC")
    List<Voucher> findActiveVouchers(@Param("now") LocalDateTime now);
}