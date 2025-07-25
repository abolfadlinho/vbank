package com.vbank.accountservice.controller;

import com.vbank.accountservice.dto.CreateAccountRequest;
import com.vbank.accountservice.dto.TransferRequest;
import com.vbank.accountservice.dto.TransferRequestWithId;
import com.vbank.accountservice.model.Account;
import com.vbank.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * POST /accounts: Creates a new bank account.
     */
    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) {
        Account newAccount = accountService.createAccount(request.userId(), request.accountType(), request.initialBalance());
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    /**
     * GET /accounts/{accountId}: Retrieves specific account details.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccountById(@PathVariable UUID accountId) {
        Account account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    /**
     * GET /users/{userId}/accounts: Lists all accounts for a user.
     * This mapping aligns with the project specification.
     */
    @GetMapping("/{userId}/accounts")
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable UUID userId) {
        List<Account> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    /**
     * PUT /accounts/transfer: Updates account balances for a fund transfer.
     */
    @PutMapping("/transfer")
    public ResponseEntity<Void> transferFunds(@RequestBody TransferRequest request) {
        accountService.transferFunds(request.fromAccountNumber(), request.toAccountNumber(), request.amount());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/transferWithId")
    public ResponseEntity<Void> transferFundsWithId(@RequestBody TransferRequestWithId request) {
        accountService.transferFundsWithId(request.fromAccountId(), request.toAccountId(), request.amount());
        return ResponseEntity.ok().build();
    }

    // The nested record definitions have been removed from here.
}