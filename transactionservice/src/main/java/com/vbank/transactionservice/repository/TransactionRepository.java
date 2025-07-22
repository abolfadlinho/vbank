package com.vbank.transactionservice.repository;

import com.vbank.transactionservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Finds all transactions where the given account ID is either the sender or the receiver.
     * @param fromAccountId The sender's account ID.
     * @param toAccountId The receiver's account ID.
     * @return A list of transactions.
     */
    List<Transaction> findByFromAccountIdOrToAccountId(UUID fromAccountId, UUID toAccountId);
}