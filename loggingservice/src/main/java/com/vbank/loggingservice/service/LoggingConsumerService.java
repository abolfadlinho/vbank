package com.vbank.loggingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vbank.loggingservice.dto.LogMessage;
import com.vbank.loggingservice.model.LogEntry;
import com.vbank.loggingservice.repository.LogEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


/**
 * Service that consumes messages from a Kafka topic and persists them. [cite: 356]
 */
@Service
public class LoggingConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConsumerService.class);
    private final LogEntryRepository logEntryRepository;

    @Autowired
    public LoggingConsumerService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    /**
     * Listens to the 'bff-logs' topic, deserializes the message, and saves it to the database.
     * @param logMessage The deserialized message from Kafka.
     */
    @KafkaListener(topics = "${vbank.kafka.topic.name}", groupId = "logging-group")
    public void consumeLog(String rawJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            LogMessage logMessage = mapper.readValue(rawJson, LogMessage.class);
            logger.info("Received log message -> {}", logMessage.getMessage());

            LogEntry logEntry = new LogEntry();
            logEntry.setMessage(logMessage.getMessage());
            logEntry.setMessageType(logMessage.getMessageType());
            logEntry.setDateTime(logMessage.getDateTime());

            logEntryRepository.save(logEntry);
            logger.info("Successfully persisted log entry to the database.");
        } catch (Exception e) {
            logger.error("Failed to process or save log message.", e);
        }
    }

}