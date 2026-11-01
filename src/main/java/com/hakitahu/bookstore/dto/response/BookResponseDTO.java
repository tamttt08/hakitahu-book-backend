package com.hakitahu.bookstore.dto.response;

import lombok.Data;

@Data
public class BookResponseDTO {
    private Integer bookId;
    private String title;
    private String author;
    private Double price;
    private Integer quantity;
    private String image;
    private String categoryName; // Chỉ trả về tên danh mục cho FE dễ hiển thị
}