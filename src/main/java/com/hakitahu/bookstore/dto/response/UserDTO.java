package com.hakitahu.bookstore.dto.response;

import java.sql.Date;

import lombok.Data;

@Data
public class UserDTO {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String provider;
    private Date createdAt;
    private Boolean isEmailVerified;
}