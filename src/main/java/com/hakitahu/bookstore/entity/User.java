package com.hakitahu.bookstore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userId;

    @Column(name = "ProviderID")
    private String providerId;
    
    @Column(name = "Username", nullable = false)
    private String username;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "FullName")
    private String fullName;

    @Column(name = "Email")
    private String email;

    @Column(name = "Provider", columnDefinition = "NVARCHAR(20) DEFAULT 'LOCAL'")
    private String provider = "LOCAL";

    @Column(name = "IsEmailVerified")
    private Boolean isEmailVerified = false;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "Address")
    private String address;

    @Column(name = "Role")
    private String role = "USER";

    @Column(name = "Status")
    private Boolean status = true;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}