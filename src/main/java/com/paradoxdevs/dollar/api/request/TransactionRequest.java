package com.paradoxdevs.dollar.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TransactionRequest {
    @NotBlank(message = "Transaction name is required.")
    private String transactionName;
    private String description;
    @NotBlank(message = "Transaction item is required.")
    private Long transactionItemId;
    @NotBlank(message = "Transaction type is required.")
    private String transactionType;
    @NotNull
    @DecimalMin(value = "0", message = "Amount must at least be 0.")
    private BigDecimal amount;
    @NotBlank(message = "Currency is required.")
    private String currency;
}
