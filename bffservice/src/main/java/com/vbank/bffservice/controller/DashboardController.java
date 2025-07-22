package com.vbank.bffservice.controller;

import com.vbank.bffservice.dto.dashboard.DashboardResponse;
import com.vbank.bffservice.dto.logging.MessageType;
import com.vbank.bffservice.service.KafkaLoggingService;
import com.vbank.bffservice.service.OrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final OrchestrationService orchestrationService;
    private final KafkaLoggingService kafkaLoggingService;

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<DashboardResponse>> getDashboard(@PathVariable String userId) {
        kafkaLoggingService.log("GET /dashboard/" + userId, MessageType.Request);
        return orchestrationService.getDashboard(userId)
                .doOnSuccess(response -> kafkaLoggingService.log(response, MessageType.Response))
                .map(ResponseEntity::ok);
    }
}