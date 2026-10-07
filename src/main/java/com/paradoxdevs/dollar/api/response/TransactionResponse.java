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
public class TransactionResponse {
    private long transactionId;
    private String transactionName;
    private String description;
    private String transactionItem;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String createdBy;
    private String createdAt;
    private String updatedBy;
    private String updatedAt;
}
