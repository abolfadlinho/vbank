package com.vbank.accountservice.dto;

import com.vbank.accountservice.model.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountRequest(UUID userId, AccountType accountType, BigDecimal initialBalance) {
}