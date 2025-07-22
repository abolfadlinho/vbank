package com.vbank.bffservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbank.bffservice.dto.logging.LogMessage;
import com.vbank.bffservice.dto.logging.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaLoggingService {

    private final KafkaTemplate<String, LogMessage> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${logging.kafka.topic}")
    private String topicName;

    public void log(Object payload, MessageType type) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            LogMessage logMessage = new LogMessage(jsonPayload, type, LocalDateTime.now());
            kafkaTemplate.send(topicName, logMessage);
            log.info("Logged {} to Kafka topic: {}", type, topicName);
        } catch (Exception e) {
            log.error("Failed to send log message to Kafka", e);
        }
    }
}