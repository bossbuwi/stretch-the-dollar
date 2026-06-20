package com.paradoxdevs.dollar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.time.Instant;

@Data
@Entity
@Immutable
@Subselect("""
    SELECT
        t.id,
        t.name,
        t.description,
        t.transaction_type,
        t.amount,
        t.currency,
        t.created_at,
        t.updated_at,
        u_created.username AS created_by_username,
        u_updated.username AS updated_by_username
    FROM transactions t
    LEFT JOIN users u_created ON t.created_by = u_created.uuid
    LEFT JOIN users u_updated ON t.updated_by = u_updated.uuid
    """)
public class TransactionWithUsername {
    @Id
    private Long id;
    private String name;
    private String description;
    private String transactionType;
    private Double amount;
    private String currency;
    @Column(name = "created_by_username")
    private String createdByUsername;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_by_username")
    private String updatedByUsername;
    @Column(name = "updated_at")
    private Instant updatedAt;
}
