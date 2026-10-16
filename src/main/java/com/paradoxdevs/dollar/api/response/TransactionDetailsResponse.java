package com.paradoxdevs.dollar.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TransactionDetailsResponse {
    private long id;
    private String name;
    private String description;
    private String currency;
    private BigDecimal amount;
    private String transactionDate;
    private String transactionCategoryName;
    private String transactionType;
    private String createdBy;
    private String createdAt;
    private String updatedBy;
    private String updatedAt;
}
