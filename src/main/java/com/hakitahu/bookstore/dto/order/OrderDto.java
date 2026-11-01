package com.hakitahu.bookstore.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Integer orderId;
    private Integer userId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime orderDate;
    private Integer shipperId;
    private List<OrderDetailDto> orderDetails; // Chứa danh sách sách trong đơn
}