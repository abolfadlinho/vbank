package com.vbank.accountservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestWithId(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
}
