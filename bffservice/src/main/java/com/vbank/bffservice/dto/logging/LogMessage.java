package com.vbank.bffservice.dto.logging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String message;
    private MessageType messageType;
    private LocalDateTime dateTime;
}