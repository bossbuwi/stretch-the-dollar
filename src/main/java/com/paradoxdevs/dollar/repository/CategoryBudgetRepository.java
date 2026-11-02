package com.paradoxdevs.dollar.repository;

import com.paradoxdevs.dollar.entity.CategoryBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
    boolean existsByTransactionCategoryId(Long transactionCategoryId);

    List<CategoryBudget> findAllByOrderByTransactionCategoryIdAscStartDateAsc();

    List<CategoryBudget> findAllByTransactionCategoryIdOrderByStartDateAsc(Long transactionCategoryId);

    Optional<CategoryBudget> findByIdAndTransactionCategoryId(Long id, Long transactionCategoryId);

    boolean existsByTransactionCategoryIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long transactionCategoryId, LocalDate endDate, LocalDate startDate);

    boolean existsByTransactionCategoryIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            Long transactionCategoryId, LocalDate endDate, LocalDate startDate, Long excludedId);
}
