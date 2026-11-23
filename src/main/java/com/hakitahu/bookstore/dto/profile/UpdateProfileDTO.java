package com.hakitahu.bookstore.dto.profile;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String fullName;
    private String email;
    private String phone;
    private String address;
}