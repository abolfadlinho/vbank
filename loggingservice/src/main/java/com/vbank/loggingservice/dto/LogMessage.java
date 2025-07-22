package com.vbank.loggingservice.dto;

import com.vbank.loggingservice.model.MessageType;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for deserializing incoming Kafka messages.
 */
@Getter
@Setter
public class LogMessage {
    private String message; // The JSON text of the request or response [cite: 360]
    private MessageType messageType; // Indicates if the log is a Request or Response [cite: 360]
}