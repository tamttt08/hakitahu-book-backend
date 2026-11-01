package com.hakitahu.bookstore.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequestDTO {
    @NotBlank(message = "Tên sách không được để trống")
    private String title;
    
    private String author;
    
    @Min(value = 0, message = "Giá tiền không hợp lệ")
    private Double price;
    
    @Min(value = 0, message = "Số lượng không hợp lệ")
    private Integer quantity;
    
    private String description;
    private String image;
    
    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;
}