package com.paradoxdevs.dollar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import java.util.UUID;

@Data
@Entity
@Table(name = "transaction_items")
@EntityListeners(AuditingEntityListener.class)
public class TransactionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private String description;
    private String currency;
    /**
     * Positive = spending cap; negative = required minimum / credit floor.
     */
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amountLimit;
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
