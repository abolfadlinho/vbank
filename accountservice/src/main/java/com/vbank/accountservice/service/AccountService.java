package com.vbank.accountservice.service;

import com.vbank.accountservice.model.Account;
import com.vbank.accountservice.model.AccountStatus;
import com.vbank.accountservice.model.AccountType;
import com.vbank.accountservice.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vbank.accountservice.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final Random random = new Random();

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Creates a new bank account for a user.
     */
    public Account createAccount(UUID userId, AccountType accountType, BigDecimal initialBalance) {
        Account account = new Account();
        account.setUserId(userId);
        account.setAccountType(accountType);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setBalance(initialBalance);
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    /**
     * Retrieves a single account by its ID.
     */
    public Account getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with ID: " + accountId));
    }

    /**
     * Retrieves all accounts for a given user.
     */
    public List<Account> getAccountsByUserId(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    /**
     * Executes a fund transfer between two accounts.
     * This operation is transactional to ensure atomicity.
     */
    @Transactional
    public void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Source account not found."));
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Destination account not found."));

        /*if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active.");
        }*/
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in the source account.");
        }

        // Perform the transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    @Transactional
    public void transferFundsWithId(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new EntityNotFoundException("Source account not found."));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new EntityNotFoundException("Destination account not found."));

        /*if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active.");
        }*/
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in the source account.");
        }

        // Perform the transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    /**
     * Scheduled job to deactivate accounts that have been idle for more than 24 hours.
     * Runs every hour as per the requirement.
     */
    @Scheduled(fixedRate = 3600000) // 3600000 ms = 1 hour
    @Transactional
    public void deactivateIdleAccounts() {
        System.out.println("Running scheduled job to deactivate idle accounts...");
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<Account> idleAccounts = accountRepository.findByStatusAndUpdatedAtBefore(AccountStatus.ACTIVE, cutoffTime);

        if (!idleAccounts.isEmpty()) {
            for (Account account : idleAccounts) {
                account.setStatus(AccountStatus.INACTIVE);
            }
            accountRepository.saveAll(idleAccounts);
            System.out.printf("Deactivated %d idle accounts.%n", idleAccounts.size());
        } else {
            System.out.println("No idle accounts found to deactivate.");
        }
    }

    /**
     * Generates a unique 10-digit account number.
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = 1_000_000_000L + random.nextLong(9_000_000_000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}