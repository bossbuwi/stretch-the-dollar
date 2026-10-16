package com.paradoxdevs.dollar.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "transaction_details_view")
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "currency")
    private String currency;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "transaction_category_name")
    private String transactionCategoryName;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "created_by_username")
    private String createdByUsername;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_by_username")
    private String updatedByUsername;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
