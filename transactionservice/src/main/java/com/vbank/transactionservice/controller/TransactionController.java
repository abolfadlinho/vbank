package com.vbank.transactionservice.controller;

import com.vbank.transactionservice.dto.TransferRequest;
import com.vbank.transactionservice.model.Transaction;
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

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer/initiation")
    public ResponseEntity<Transaction> initiateTransfer(@RequestBody TransferRequest request) {
        Transaction transaction = transactionService.initiateTransfer(request);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @PostMapping("/transfer/execution/{transactionId}")
    public ResponseEntity<Transaction> executeTransfer(@PathVariable UUID transactionId) {
        Transaction transaction = transactionService.executeTransfer(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<Transaction>> getAccountTransactions(@PathVariable UUID accountId) {
        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountId);
        return ResponseEntity.ok(transactions);
    }
}