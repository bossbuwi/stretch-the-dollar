package com.paradoxdevs.dollar.repository;

import com.paradoxdevs.dollar.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.transactionCategoryId = :categoryId
              and t.transactionDate between :startDate and :endDate
            """)
    BigDecimal sumAmountForCategoryAndDateRange(@Param("categoryId") Long categoryId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
