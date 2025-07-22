package com.vbank.loggingservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a single log entry in the 'logs' table. [cite: 358]
 */
@Entity
@Table(name = "logs")
@Getter
@Setter
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // [cite: 360]

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message; // [cite: 360]

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, columnDefinition = "message_type")
    private MessageType messageType; // [cite: 360]

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime; // [cite: 360]
}