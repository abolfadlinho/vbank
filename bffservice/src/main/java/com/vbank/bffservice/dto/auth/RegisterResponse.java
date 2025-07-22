package com.vbank.bffservice.dto.auth;

import lombok.Data;

@Data
public class RegisterResponse {
    private String userId;
    private String username;
    private String message;
}