package com.paradoxdevs.dollar.api.response;

import com.paradoxdevs.dollar.entity.BudgetRecurrenceFrequency;
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
public class CategoryBudgetResponse {
    private Long id;
    private Long transactionCategoryId;
    private BigDecimal budgetAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetRecurrenceFrequency recurrenceFrequency;
    private Integer customPeriodDays;
}
