package com.vbank.accountservice.repository;

import com.vbank.accountservice.model.Account;
import com.vbank.accountservice.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Finds all accounts associated with a specific user ID.
     */
    List<Account> findByUserId(UUID userId);

    /**
     * Finds an account by its unique account number.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Checks if an account number already exists.
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Finds active accounts that have not been updated since the given cutoff time.
     * Used by the scheduled job to identify and deactivate idle accounts.
     */
    List<Account> findByStatusAndUpdatedAtBefore(AccountStatus status, LocalDateTime cutoffTime);
}