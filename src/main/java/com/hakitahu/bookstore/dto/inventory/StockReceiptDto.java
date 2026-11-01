package com.hakitahu.bookstore.dto.inventory;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class StockReceiptDto {
    private Integer receiptId;
    private Integer createdBy;
    private String adminName;
    private LocalDateTime receiptDate;
    private String note;
    private List<StockReceiptDetailDto> details;
}