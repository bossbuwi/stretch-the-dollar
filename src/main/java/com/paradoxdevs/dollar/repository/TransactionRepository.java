package com.paradoxdevs.dollar.repository;

import com.paradoxdevs.dollar.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
