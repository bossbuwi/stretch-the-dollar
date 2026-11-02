package com.paradoxdevs.dollar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "category_budgets")
@EntityListeners(AuditingEntityListener.class)
public class CategoryBudget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long transactionCategoryId;

    @Column(name = "budget_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal budgetAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_frequency")
    private BudgetRecurrenceFrequency recurrenceFrequency;

    @Column(name = "custom_period_days")
    private Integer customPeriodDays;

    @CreatedBy
    @Column(updatable = false, nullable = false)
    private UUID createdBy;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(nullable = false)
    private UUID updatedBy;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
