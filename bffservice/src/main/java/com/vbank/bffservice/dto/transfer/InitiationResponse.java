package com.vbank.bffservice.dto.transfer;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InitiationResponse {
    private String transactionId;
    private String status;
    private LocalDateTime timestamp;
}