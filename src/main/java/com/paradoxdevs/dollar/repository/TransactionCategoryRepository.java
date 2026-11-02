package com.paradoxdevs.dollar.repository;

import com.paradoxdevs.dollar.entity.TransactionCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select category from TransactionCategory category where category.id = :id")
    Optional<TransactionCategory> findByIdForUpdate(@Param("id") Long id);
}
