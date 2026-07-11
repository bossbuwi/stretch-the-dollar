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

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String transactionType;
    private Double amount;
    private String currency;
    @CreatedBy
    @Column(updatable = false)
    private UUID createdBy;
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Instant createdAt;
    @LastModifiedBy
    private UUID updatedBy;
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
