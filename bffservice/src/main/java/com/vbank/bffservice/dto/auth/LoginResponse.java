package com.vbank.bffservice.dto.auth;

import lombok.Data;

@Data
public class LoginResponse {
    private String userId;
    private String username;
}