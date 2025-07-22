package com.vbank.bffservice.dto.transfer;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransferResponse {
    private String transactionId;
    private String status;
    private LocalDateTime timestamp;
    private String message; // Custom message for BFF response
}