package com.paradoxdevs.dollar.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TransactionResponse {
    private long transactionId;
    private String transactionName;
    private String description;
    private String transactionType;
    private double amount;
    private String currency;
}
