package com.vbank.bffservice.controller;

import com.vbank.bffservice.dto.logging.MessageType;
import com.vbank.bffservice.dto.transfer.TransferRequest;
import com.vbank.bffservice.dto.transfer.TransferResponse;
import com.vbank.bffservice.service.KafkaLoggingService;
import com.vbank.bffservice.service.OrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final OrchestrationService orchestrationService;
    private final KafkaLoggingService kafkaLoggingService;

    @PostMapping
    public Mono<ResponseEntity<TransferResponse>> transferFunds(@RequestBody TransferRequest request) {
        kafkaLoggingService.log(request, MessageType.Request);
        return orchestrationService.executeTransfer(request)
                .doOnSuccess(response -> kafkaLoggingService.log(response, MessageType.Response))
                .map(ResponseEntity::ok);
    }
}