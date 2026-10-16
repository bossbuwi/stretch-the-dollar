package com.paradoxdevs.dollar.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class TransactionCategoryResponse {
    private long id;
    private String name;
    private String description;
    private String transactionType;
}
