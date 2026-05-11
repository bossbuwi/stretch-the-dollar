package com.paradoxdevs.dollar.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TransactionRequest {
    private String transactionName;
    private String description;
    private String transactionType;
    private double amount;
    private String currency;
}
