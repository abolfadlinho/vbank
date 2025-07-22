package com.vbank.transactionservice.service;

import com.vbank.transactionservice.dto.TransferRequest;
import com.vbank.transactionservice.model.Transaction;
import com.vbank.transactionservice.model.TransactionStatus;
import com.vbank.transactionservice.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Value("${account.service.url}")
    private String accountServiceUrl;

    public TransactionService(TransactionRepository transactionRepository, RestTemplate restTemplate) {
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Initiates a new transfer by creating a transaction record with INITIATED status.
     */
    public Transaction initiateTransfer(TransferRequest request) {
        Transaction transaction = Transaction.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransactionStatus.INITIATED)
                .build();
        return transactionRepository.save(transaction);
    }

    /**
     * Executes a previously initiated transfer.
     * It calls the Account Service to perform the actual balance update.
     */
    public Transaction executeTransfer(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.INITIATED) {
            throw new IllegalStateException("Transaction cannot be executed. Current status: " + transaction.getStatus());
        }

        try {
            // Prepare the request for the Account Service
            TransferRequest accountTransferRequest = new TransferRequest();
            accountTransferRequest.setFromAccountId(transaction.getFromAccountId());
            accountTransferRequest.setToAccountId(transaction.getToAccountId());
            accountTransferRequest.setAmount(transaction.getAmount());

            HttpEntity<TransferRequest> requestEntity = new HttpEntity<>(accountTransferRequest);

            // Call the Account Service's /accounts/transfer endpoint
            restTemplate.exchange(
                    accountServiceUrl + "/transfer",
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class // We don't expect a body in the response
            );

            // If the call succeeds, update the status to SUCCESS
            transaction.setStatus(TransactionStatus.SUCCESS);

        } catch (HttpClientErrorException e) {
            // If the Account Service returns an error (e.g., insufficient funds), mark as FAILED
            transaction.setStatus(TransactionStatus.FAILED);
            // Optionally, log e.getResponseBodyAsString() for more details
        } catch (Exception e) {
            // For other errors (e.g., network issues), mark as FAILED
            transaction.setStatus(TransactionStatus.FAILED);
        }

        return transactionRepository.save(transaction);
    }

    /**
     * Retrieves all transactions related to a specific account.
     */
    public List<Transaction> getTransactionsForAccount(UUID accountId) {
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }
}