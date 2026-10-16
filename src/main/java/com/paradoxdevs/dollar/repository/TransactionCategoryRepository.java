package com.paradoxdevs.dollar.repository;

import com.paradoxdevs.dollar.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {
}
