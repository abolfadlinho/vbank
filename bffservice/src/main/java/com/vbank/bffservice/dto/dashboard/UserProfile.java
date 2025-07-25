package com.vbank.bffservice.dto.dashboard;

import lombok.Data;

@Data
public class UserProfile {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}