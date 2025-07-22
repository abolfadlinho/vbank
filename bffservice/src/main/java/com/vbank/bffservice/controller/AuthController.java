package com.vbank.bffservice.controller;

import com.vbank.bffservice.dto.auth.LoginRequest;
import com.vbank.bffservice.dto.auth.LoginResponse;
import com.vbank.bffservice.dto.auth.RegisterRequest;
import com.vbank.bffservice.dto.auth.RegisterResponse;
import com.vbank.bffservice.dto.logging.MessageType;
import com.vbank.bffservice.service.KafkaLoggingService;
import com.vbank.bffservice.service.OrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OrchestrationService orchestrationService;
    private final KafkaLoggingService kafkaLoggingService;

    @PostMapping("/register")
    public Mono<ResponseEntity<RegisterResponse>> register(@RequestBody RegisterRequest request) {
        kafkaLoggingService.log(request, MessageType.Request);
        return orchestrationService.registerUser(request)
                .doOnSuccess(response -> kafkaLoggingService.log(response, MessageType.Response))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        kafkaLoggingService.log(request, MessageType.Request);
        return orchestrationService.loginUser(request)
                .doOnSuccess(response -> kafkaLoggingService.log(response, MessageType.Response))
                .map(ResponseEntity::ok);
    }
}