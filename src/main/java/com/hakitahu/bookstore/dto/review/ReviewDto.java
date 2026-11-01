package com.hakitahu.bookstore.dto.review;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Integer reviewId;
    private Integer userId;
    private String fullName; // Lấy từ bảng User để hiển thị tên người bình luận
    private Integer orderDetailId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}