package com.vbank.transactionservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferRequest {
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String description;
}