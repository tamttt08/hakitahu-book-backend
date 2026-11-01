package com.hakitahu.bookstore.dto.inventory;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StockReceiptDetailDto {
    private Integer receiptDetailId;
    private Integer bookId;
    private String bookTitle;
    private Integer quantity;
    private BigDecimal importPrice;
}
