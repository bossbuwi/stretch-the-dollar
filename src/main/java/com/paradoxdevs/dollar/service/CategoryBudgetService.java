package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.CategoryBudgetRequest;
import com.paradoxdevs.dollar.api.response.CategoryBudgetResponse;
import com.paradoxdevs.dollar.api.response.CategoryBudgetSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface CategoryBudgetService {
    List<CategoryBudgetResponse> getBudgets();
    List<CategoryBudgetResponse> getBudgetsForCategory(Long categoryId);
    CategoryBudgetResponse getBudget(Long budgetId);
    CategoryBudgetSummaryResponse getBudgetSummary(Long budgetId, LocalDate asOfDate);
    CategoryBudgetResponse addBudget(Long categoryId, CategoryBudgetRequest request);
    CategoryBudgetResponse updateBudget(Long categoryId, Long budgetId, CategoryBudgetRequest request);
    CategoryBudgetResponse deleteBudget(Long categoryId, Long budgetId);
}
