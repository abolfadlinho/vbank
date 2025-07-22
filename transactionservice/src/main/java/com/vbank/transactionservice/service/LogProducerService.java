package com.vbank.transactionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LogProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public LogProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    private static final String TOPIC = "system_logs";

    public void sendLog(String message, String messageType) {
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("message", message);
            logMap.put("messageType", messageType); // "Request" or "Response"
            logMap.put("dateTime", LocalDateTime.now().toString());

            String jsonLog = objectMapper.writeValueAsString(logMap);
            kafkaTemplate.send(TOPIC, jsonLog);
        } catch (Exception e) {
            // In a real application, handle this exception properly (e.g., log to a fallback file)
            System.err.println("Error sending log to Kafka: " + e.getMessage());
        }
    }
}