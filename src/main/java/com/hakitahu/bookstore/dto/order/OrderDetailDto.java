package com.hakitahu.bookstore.dto.order;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderDetailDto {
    private Integer orderDetailId;
    private Integer bookId;
    private String bookTitle;
    private Integer quantity;
    private BigDecimal price;
}