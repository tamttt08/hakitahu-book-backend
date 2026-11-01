package com.hakitahu.bookstore.dto.delivery;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DeliveryProofDto {
    private Integer proofId;
    private Integer orderId;
    private Integer shipperId;
    private String imageUrl;
    private LocalDateTime createdAt;
}
