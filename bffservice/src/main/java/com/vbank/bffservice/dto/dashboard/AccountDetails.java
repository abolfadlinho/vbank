package com.vbank.bffservice.dto.dashboard;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountDetails {
    private String accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private List<TransactionDetails> transactions;
}