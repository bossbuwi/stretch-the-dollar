package com.paradoxdevs.dollar.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TransactionCategoryRequest {
    @NotBlank(message = "field: name is required.")
    private String name;
    private String description;
    @NotBlank(message = "field: transactionType is required.")
    private String transactionType;
}
