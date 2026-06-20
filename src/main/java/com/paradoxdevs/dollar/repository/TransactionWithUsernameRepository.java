package com.paradoxdevs.dollar.repository;

import com.paradoxdevs.dollar.entity.TransactionWithUsername;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionWithUsernameRepository extends JpaRepository<TransactionWithUsername, Long> {
}
