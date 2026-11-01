package com.hakitahu.bookstore.dto.response;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private String type = "Bearer";
    private UserDTO user;
}