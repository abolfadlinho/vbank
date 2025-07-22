package com.vbank.bffservice.dto.transfer;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String description;
}