package com.hakitahu.bookstore.dto.cart;

import java.util.List;

import lombok.Data;

@Data
public class CartDTO {
    private Integer cartId;
    private Integer userId;
    private Double cartTotal; // Tổng tiền của cả giỏ
    private List<CartItemDTO> items;
}