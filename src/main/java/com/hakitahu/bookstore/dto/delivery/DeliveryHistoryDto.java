package com.hakitahu.bookstore.dto.delivery;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeliveryHistoryDto {
    private Integer deliveryId;
    private Integer orderId;
    private Integer shipperId;
    private String status;
    private String note;
    private LocalDateTime updatedAt;
}