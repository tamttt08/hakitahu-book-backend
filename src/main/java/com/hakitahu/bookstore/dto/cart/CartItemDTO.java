package com.hakitahu.bookstore.dto.cart;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Integer cartItemId;
    private Integer bookId;
    private String bookTitle;
    private String bookImage;
    private Integer quantity;
    private BigDecimal price;
}