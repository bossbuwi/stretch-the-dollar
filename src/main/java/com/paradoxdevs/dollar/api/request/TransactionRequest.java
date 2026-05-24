package com.paradoxdevs.dollar.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TransactionRequest {
    @NotBlank(message = "Transaction name is required.")
    private String transactionName;
    private String description;
    @NotBlank(message = "Transaction type is required.")
    private String transactionType;
    @NotNull
    @Min(value = 1, message = "Amount must at least be 1.")
    private Double amount;
    @NotNull
    @NotBlank(message = "Currency is required.")
    private String currency;
}
