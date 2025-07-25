package com.vbank.transactionservice.controller;

import com.vbank.transactionservice.dto.TransferRequest;
import com.vbank.transactionservice.model.Transaction;
import com.vbank.transactionservice.service.LogProducerService;
import com.vbank.transactionservice.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final LogProducerService logProducerService;

    public TransactionController(TransactionService transactionService, LogProducerService logProducerService) {
        this.transactionService = transactionService;
        this.logProducerService = logProducerService;
    }

    @PostMapping("/transfer/initiation")
    public ResponseEntity<Transaction> initiateTransfer(@RequestBody TransferRequest request) {
        //logProducerService.sendLog("Initiate transfer request received: " + request.toString(), "Request");
        Transaction transaction = transactionService.initiateTransfer(request);
        //logProducerService.sendLog("Transfer initiated response: " + transaction.toString(), "Response");
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @PostMapping("/transfer/execution/{transactionId}")
    public ResponseEntity<Transaction> executeTransfer(@PathVariable UUID transactionId) {
        //logProducerService.sendLog("Execute transfer request for ID: " + transactionId, "Request");
        Transaction transaction = transactionService.executeTransfer(transactionId);
        //logProducerService.sendLog("Execute transfer response: " + transaction.toString(), "Response");
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<Transaction>> getAccountTransactions(@PathVariable UUID accountId) {
        //logProducerService.sendLog("Get transactions request for Account ID: " + accountId, "Request");
        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountId);
        //logProducerService.sendLog("Get transactions response count: " + transactions.size(), "Response");
        return ResponseEntity.ok(transactions);
    }
}