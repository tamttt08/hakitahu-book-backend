package com.hakitahu.bookstore.repository;

import com.hakitahu.bookstore.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findAllByOrderByPromotionIdDesc();

    // Dùng cho trang Khuyến mãi ngoài cửa hàng (chỉ lấy đợt sale đang hiệu lực)
    @Query("SELECT DISTINCT p FROM Promotion p LEFT JOIN FETCH p.books WHERE p.status = true AND p.startDate <= :now AND p.endDate >= :now ORDER BY p.endDate ASC")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
}