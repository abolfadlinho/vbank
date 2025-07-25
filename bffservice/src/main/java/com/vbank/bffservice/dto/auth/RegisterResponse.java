package com.vbank.bffservice.dto.auth;

import lombok.Data;

@Data
public class RegisterResponse {
    private String id;
    private String username;
    private String message = "User registered successfully.";
}