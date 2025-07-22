package com.vbank.bffservice.dto.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDetails {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
}