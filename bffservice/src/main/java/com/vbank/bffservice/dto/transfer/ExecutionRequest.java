package com.vbank.bffservice.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExecutionRequest {
    private String transactionId;
}