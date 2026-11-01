package com.hakitahu.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "VerificationTokens")
public class VerificationToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TokenID")
    private Integer tokenId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "Token", nullable = false)
    private String token;

    @Column(name = "ExpiryDate", nullable = false)
    private LocalDateTime expiryDate;
}