package com.paradoxdevs.dollar.api.request;

import com.paradoxdevs.dollar.entity.BudgetRecurrenceFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CategoryBudgetRequest {
    @NotNull
    @DecimalMin(value = "0.0001", message = "Budget amount must be greater than zero.")
    @Digits(integer = 15, fraction = 4, message = "Budget amount supports up to 15 integer and 4 fractional digits.")
    private BigDecimal budgetAmount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private BudgetRecurrenceFrequency recurrenceFrequency;

    @Min(value = 1, message = "Custom recurrence period must be at least one day.")
    private Integer customPeriodDays;
}
