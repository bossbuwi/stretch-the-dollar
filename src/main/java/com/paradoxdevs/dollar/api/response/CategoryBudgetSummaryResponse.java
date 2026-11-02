package com.paradoxdevs.dollar.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class CategoryBudgetSummaryResponse {
    private Long budgetId;
    private Long transactionCategoryId;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
}
