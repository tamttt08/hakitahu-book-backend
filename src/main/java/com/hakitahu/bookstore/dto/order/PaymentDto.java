package com.hakitahu.bookstore.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentDto {
    private Integer paymentId;
    private Integer orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private String paymentStatus;
    private String transactionCode;
    private LocalDateTime paidAt;
}