package com.hakitahu.bookstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Vouchers")
@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VoucherID")
    private Integer voucherId;

    @Column(name = "Code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "DiscountType", nullable = false, length = 20)
    private String discountType; // "PERCENT" hoặc "FIXED"

    @Column(name = "DiscountValue", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "MinOrderValue", precision = 10, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "MaxDiscount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "UsedCount")
    private Integer usedCount = 0;

    @Column(name = "StartDate", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "Status")
    private Boolean status = true;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.usedCount == null) {
            this.usedCount = 0;
        }
        if (this.minOrderValue == null) {
            this.minOrderValue = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = true;
        }
    }
}