package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.request.CategoryBudgetRequest;
import com.paradoxdevs.dollar.api.response.CategoryBudgetResponse;
import com.paradoxdevs.dollar.api.response.CategoryBudgetSummaryResponse;
import com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics;
import com.paradoxdevs.dollar.aspect.annotation.WithPermission;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.exception.BudgetRuleException;
import com.paradoxdevs.dollar.service.CategoryBudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WithPermission(notAllowedRole = Role.BANNED)
@PerformanceMetrics
@RestController
@RequestMapping("/budget")
public class CategoryBudgetController {
    private final CategoryBudgetService categoryBudgetService;

    public CategoryBudgetController(CategoryBudgetService categoryBudgetService) {
        this.categoryBudgetService = categoryBudgetService;
    }

    @GetMapping("/index")
    public ResponseEntity<List<CategoryBudgetResponse>> getBudgets() {
        return ResponseEntity.ok(categoryBudgetService.getBudgets());
    }

    @GetMapping("/category/{categoryId}/index")
    public ResponseEntity<List<CategoryBudgetResponse>> getBudgetsForCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryBudgetService.getBudgetsForCategory(categoryId));
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<CategoryBudgetResponse> getBudget(@PathVariable Long budgetId) {
        return ResponseEntity.ok(categoryBudgetService.getBudget(budgetId));
    }

    @GetMapping("/{budgetId}/summary")
    public ResponseEntity<CategoryBudgetSummaryResponse> getBudgetSummary(
            @PathVariable Long budgetId,
            @RequestParam(name = "date", required = false) String date) {
        return ResponseEntity.ok(categoryBudgetService.getBudgetSummary(budgetId, parseDate(date)));
    }

    @WithPermission(notAllowedRole = {Role.RESTRICTED, Role.BANNED})
    @PostMapping("/category/{categoryId}")
    public ResponseEntity<CategoryBudgetResponse> addBudget(
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryBudgetRequest request) {
        return ResponseEntity.ok(categoryBudgetService.addBudget(categoryId, request));
    }

    @WithPermission(notAllowedRole = {Role.RESTRICTED, Role.BANNED})
    @PutMapping("/category/{categoryId}/{budgetId}")
    public ResponseEntity<CategoryBudgetResponse> updateBudget(
            @PathVariable Long categoryId,
            @PathVariable Long budgetId,
            @RequestBody @Valid CategoryBudgetRequest request) {
        return ResponseEntity.ok(categoryBudgetService.updateBudget(categoryId, budgetId, request));
    }

    @WithPermission(notAllowedRole = {Role.RESTRICTED, Role.BANNED})
    @DeleteMapping("/category/{categoryId}/{budgetId}")
    public ResponseEntity<CategoryBudgetResponse> deleteBudget(
            @PathVariable Long categoryId,
            @PathVariable Long budgetId) {
        return ResponseEntity.ok(categoryBudgetService.deleteBudget(categoryId, budgetId));
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw new BudgetRuleException(ErrorCode.REQUEST_VALIDATION_ERROR,
                    "The summary date must use ISO format yyyy-MM-dd.");
        }
    }
}
