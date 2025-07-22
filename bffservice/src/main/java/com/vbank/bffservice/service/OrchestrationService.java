package com.vbank.bffservice.service;

import com.vbank.bffservice.dto.auth.*;
import com.vbank.bffservice.dto.dashboard.*;
import com.vbank.bffservice.dto.transfer.*;
import com.vbank.bffservice.exception.DownstreamServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrchestrationService {

    private final WebClient webClient;

    @Value("${service.user.url}")
    private String userServiceUrl;

    @Value("${service.account.url}")
    private String accountServiceUrl;

    @Value("${service.transaction.url}")
    private String transactionServiceUrl;

    // --- Authentication Orchestration ---

    public Mono<RegisterResponse> registerUser(RegisterRequest request) {
        return webClient.post()
                .uri(userServiceUrl + "/users/register")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse ->
                        Mono.error(new DownstreamServiceException("User Service", clientResponse.statusCode())))
                .bodyToMono(RegisterResponse.class);
    }

    public Mono<LoginResponse> loginUser(LoginRequest request) {
        return webClient.post()
                .uri(userServiceUrl + "/users/login")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse ->
                        Mono.error(new DownstreamServiceException("User Service", clientResponse.statusCode())))
                .bodyToMono(LoginResponse.class);
    }

    // --- Dashboard Orchestration ---

    public Mono<DashboardResponse> getDashboard(String userId) {
        // 1. Fetch User Profile [cite: 278]
        Mono<UserProfile> userProfileMono = webClient.get()
                .uri(userServiceUrl + "/users/{userId}/profile", userId)
                .retrieve()
                .bodyToMono(UserProfile.class);

        // 2. Fetch Accounts [cite: 279]
        Mono<List<AccountDetails>> accountsMono = webClient.get()
                .uri(accountServiceUrl + "/{userId}/accounts", userId)
                .retrieve()
                .bodyToFlux(AccountDetails.class)
                .collectList();

        // 3. Combine user profile and accounts, then fetch transactions for each account
        return Mono.zip(userProfileMono, accountsMono)
                .flatMap(tuple -> {
                    UserProfile profile = tuple.getT1();
                    List<AccountDetails> accounts = tuple.getT2();

                    // For each account, fetch its transactions asynchronously [cite: 280-281]
                    return Flux.fromIterable(accounts)
                            .flatMap(this::getTransactionsForAccount)
                            .collectList()
                            .map(accountsWithTransactions -> new DashboardResponse(profile, accountsWithTransactions));
                })
                .switchIfEmpty(Mono.error(new DownstreamServiceException("User not found", HttpStatus.NOT_FOUND)));
    }

    private Mono<AccountDetails> getTransactionsForAccount(AccountDetails account) {
        return webClient.get()
                .uri(transactionServiceUrl + "/{accountId}/transactions", account.getAccountId())
                .retrieve()
                .bodyToFlux(TransactionDetails.class)
                .collectList()
                .map(transactions -> {
                    account.setTransactions(transactions);
                    return account;
                })
                .defaultIfEmpty(account); // Return account even if no transactions are found
    }

    // --- Transfer Orchestration ---

    public Mono<TransferResponse> executeTransfer(TransferRequest request) {
        // 1. Call Initiation Service [cite: 333]
        return webClient.post()
                .uri(transactionServiceUrl + "/transfer/initiation")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::isError, resp -> Mono.error(new DownstreamServiceException("Transaction Initiation Failed", resp.statusCode())))
                .bodyToMono(InitiationResponse.class)
                // 2. If initiation is successful, call Execution Service [cite: 334-335]
                .flatMap(initiationResponse -> webClient.post()
                        .uri(transactionServiceUrl + "/transfer/execution")
                        .bodyValue(new ExecutionRequest(initiationResponse.getTransactionId()))
                        .retrieve()
                        .onStatus(HttpStatus::isError, resp -> Mono.error(new DownstreamServiceException("Transaction Execution Failed", resp.statusCode())))
                        .bodyToMono(TransferResponse.class));
    }
}