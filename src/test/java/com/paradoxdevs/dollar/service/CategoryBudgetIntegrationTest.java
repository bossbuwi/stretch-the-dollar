package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.CategoryBudgetRequest;
import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.CategoryBudgetResponse;
import com.paradoxdevs.dollar.api.response.CategoryBudgetSummaryResponse;
import com.paradoxdevs.dollar.entity.BudgetRecurrenceFrequency;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.exception.BudgetRuleException;
import com.paradoxdevs.dollar.repository.CategoryBudgetRepository;
import com.paradoxdevs.dollar.repository.TransactionCategoryRepository;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class CategoryBudgetIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;
    @Autowired
    private CategoryBudgetRepository categoryBudgetRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CategoryBudgetService categoryBudgetService;
    @Autowired
    private TransactionCategoryService transactionCategoryService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("One-off summary includes range boundaries and aggregates all currencies and users")
    void oneOffSummaryIncludesInclusiveBoundariesAndAllUsers() {
        User categoryOwner = saveUser("budget-owner");
        User otherUser = saveUser("budget-transaction-user");
        TransactionCategory category = saveCategory(categoryOwner, "Groceries", "EXPENSE");
        setAuthentication(categoryOwner.getUuid(), Role.USER);
        CategoryBudgetResponse budget = categoryBudgetService.addBudget(category.getId(),
                request("100.00", "2026-06-01", "2026-06-30", null, null));

        setAuthentication(otherUser.getUuid(), Role.USER);
        saveTransaction(category.getId(), "10.00", "USD", "2026-06-01");
        saveTransaction(category.getId(), "15.00", "EUR", "2026-06-30");
        saveTransaction(category.getId(), "8.00", "USD", "2026-05-31");
        saveTransaction(category.getId(), "9.00", "USD", "2026-07-01");

        CategoryBudgetSummaryResponse summary = categoryBudgetService.getBudgetSummary(
                budget.getId(), LocalDate.parse("2026-06-15"));

        assertEquals(LocalDate.parse("2026-06-01"), summary.getPeriodStartDate());
        assertEquals(LocalDate.parse("2026-06-30"), summary.getPeriodEndDate());
        assertEquals(0, new BigDecimal("25.00").compareTo(summary.getSpentAmount()));
        assertEquals(0, new BigDecimal("75.00").compareTo(summary.getRemainingAmount()));
    }

    @Test
    @DisplayName("Weekly recurrence returns the anchored seven-day period")
    void weeklyRecurrenceCalculatesAnchoredPeriod() {
        User owner = saveUser("weekly-budget-owner");
        TransactionCategory category = saveCategory(owner, "Transport", "EXPENSE");
        setAuthentication(owner.getUuid(), Role.USER);

        CategoryBudgetResponse budget = categoryBudgetService.addBudget(category.getId(),
                request("80.00", "2026-06-03", "2026-06-20", BudgetRecurrenceFrequency.WEEKLY, null));
        CategoryBudgetSummaryResponse summary = categoryBudgetService.getBudgetSummary(
                budget.getId(), LocalDate.parse("2026-06-09"));

        assertEquals(LocalDate.parse("2026-06-03"), summary.getPeriodStartDate());
        assertEquals(LocalDate.parse("2026-06-09"), summary.getPeriodEndDate());

        CategoryBudgetSummaryResponse finalPeriod = categoryBudgetService.getBudgetSummary(
                budget.getId(), LocalDate.parse("2026-06-19"));
        assertEquals(LocalDate.parse("2026-06-17"), finalPeriod.getPeriodStartDate());
        assertEquals(LocalDate.parse("2026-06-20"), finalPeriod.getPeriodEndDate());
        assertThrows(BudgetRuleException.class, () -> categoryBudgetService.getBudgetSummary(
                budget.getId(), LocalDate.parse("2026-06-02")));
    }

    @Test
    @DisplayName("An unused budget reports zero spending and its full remaining amount")
    void unusedBudgetStartsWithZeroSpend() {
        User owner = saveUser("unused-budget-owner");
        TransactionCategory category = saveCategory(owner, "Household", "EXPENSE");
        setAuthentication(owner.getUuid(), Role.USER);
        CategoryBudgetResponse budget = categoryBudgetService.addBudget(category.getId(),
                request("250.00", "2026-06-01", "2026-06-30", null, null));

        CategoryBudgetSummaryResponse summary = categoryBudgetService.getBudgetSummary(
                budget.getId(), LocalDate.parse("2026-06-15"));

        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getSpentAmount()));
        assertEquals(0, new BigDecimal("250.00").compareTo(summary.getRemainingAmount()));
    }

    @Test
    @DisplayName("Monthly recurrence handles month-end anchors")
    void monthlyRecurrenceHandlesMonthEnd() {
        User owner = saveUser("monthly-budget-owner");
        TransactionCategory category = saveCategory(owner, "Bills", "EXPENSE");
        setAuthentication(owner.getUuid(), Role.USER);

        CategoryBudgetResponse budget = categoryBudgetService.addBudget(category.getId(),
                request("150.00", "2026-01-31", "2026-04-30", BudgetRecurrenceFrequency.MONTHLY, null));
        CategoryBudgetSummaryResponse summary = categoryBudgetService.getBudgetSummary(
                budget.getId(), LocalDate.parse("2026-02-28"));

        assertEquals(LocalDate.parse("2026-02-28"), summary.getPeriodStartDate());
        assertEquals(LocalDate.parse("2026-03-30"), summary.getPeriodEndDate());
    }

    @Test
    @DisplayName("Custom recurrence uses the requested number of days")
    void customRecurrenceUsesConfiguredPeriod() {
        User owner = saveUser("custom-budget-owner");
        TransactionCategory category = saveCategory(owner, "Dining", "EXPENSE");
        setAuthentication(owner.getUuid(), Role.USER);

        CategoryBudgetResponse budget = categoryBudgetService.addBudget(category.getId(),
                request("200.00", "2026-06-03", "2026-06-30", BudgetRecurrenceFrequency.CUSTOM_DAYS, 10));
        CategoryBudgetSummaryResponse summary = categoryBudgetService.getBudgetSummary(
                budget.getId(), LocalDate.parse("2026-06-12"));

        assertEquals(LocalDate.parse("2026-06-12"), summary.getPeriodEndDate());
    }

    @Test
    @DisplayName("Yearly recurrence calculates the anchored annual period")
    void yearlyRecurrenceCalculatesAnchoredPeriod() {
        User owner = saveUser("yearly-budget-owner");
        TransactionCategory category = saveCategory(owner, "Insurance", "EXPENSE");
        setAuthentication(owner.getUuid(), Role.USER);

        CategoryBudgetResponse budget = categoryBudgetService.addBudget(category.getId(),
                request("500.00", "2024-02-29", "2028-03-01", BudgetRecurrenceFrequency.YEARLY, null));
        CategoryBudgetSummaryResponse summary = categoryBudgetService.getBudgetSummary(
                budget.getId(), LocalDate.parse("2025-03-01"));

        assertEquals(LocalDate.parse("2025-02-28"), summary.getPeriodStartDate());
        assertEquals(LocalDate.parse("2026-02-27"), summary.getPeriodEndDate());
    }

    @Test
    @DisplayName("Overlapping date ranges for the same category are rejected")
    void overlappingRangesAreRejected() {
        User owner = saveUser("overlap-budget-owner");
        TransactionCategory category = saveCategory(owner, "Fuel", "EXPENSE");
        setAuthentication(owner.getUuid(), Role.USER);
        categoryBudgetService.addBudget(category.getId(),
                request("100.00", "2026-06-01", "2026-06-30", null, null));

        BudgetRuleException exception = assertThrows(BudgetRuleException.class,
                () -> categoryBudgetService.addBudget(category.getId(),
                        request("200.00", "2026-06-30", "2026-07-30", null, null)));

        assertEquals(ErrorCode.BUDGET_PERIOD_OVERLAP, exception.getErrorCode());
    }

    @Test
    @DisplayName("Income categories cannot have budgets")
    void incomeCategoryIsRejected() {
        User owner = saveUser("income-budget-owner");
        TransactionCategory category = saveCategory(owner, "Salary", "INCOME");
        setAuthentication(owner.getUuid(), Role.USER);

        assertThrows(BudgetRuleException.class, () -> categoryBudgetService.addBudget(category.getId(),
                request("100.00", "2026-06-01", "2026-06-30", null, null)));
    }

    @Test
    @DisplayName("Category with budgets cannot be changed to income")
    void categoryWithBudgetCannotChangeToIncome() {
        User owner = saveUser("budget-category-type-owner");
        TransactionCategory category = saveCategory(owner, "Dining out", "EXPENSE");
        setAuthentication(owner.getUuid(), Role.USER);
        categoryBudgetService.addBudget(category.getId(),
                request("300.00", "2026-06-01", "2026-06-30", null, null));

        assertThrows(BudgetRuleException.class, () -> transactionCategoryService.updateTransactionItem(
                category.getId(), TransactionCategoryRequest.builder()
                        .name("Dining income")
                        .transactionType("INCOME")
                        .build()));

        assertEquals("EXPENSE", transactionCategoryRepository.findById(category.getId()).orElseThrow()
                .getTransactionType());
    }

    @Test
    @DisplayName("Only the category owner or an admin can manage its budgets")
    void budgetChangesRequireCategoryOwnerOrAdmin() {
        User owner = saveUser("authorized-budget-owner");
        User otherUser = saveUser("unauthorized-budget-user");
        TransactionCategory category = saveCategory(owner, "Books", "EXPENSE");
        setAuthentication(otherUser.getUuid(), Role.USER);

        assertThrows(AccessDeniedException.class, () -> categoryBudgetService.addBudget(category.getId(),
                request("100.00", "2026-06-01", "2026-06-30", null, null)));

        setAuthentication(saveUser("budget-admin").getUuid(), Role.ADMIN);
        CategoryBudgetResponse created = categoryBudgetService.addBudget(category.getId(),
                request("100.00", "2026-06-01", "2026-06-30", null, null));
        CategoryBudgetResponse deleted = categoryBudgetService.deleteBudget(category.getId(), created.getId());

        assertEquals(created.getId(), deleted.getId());
        assertFalse(categoryBudgetRepository.existsById(created.getId()));
    }

    @Test
    @DisplayName("Updating a budget excludes its existing range from overlap checks")
    void updatingBudgetDoesNotConflictWithItself() {
        User owner = saveUser("self-overlap-budget-owner");
        TransactionCategory category = saveCategory(owner, "Utilities", "EXPENSE");
        setAuthentication(owner.getUuid(), Role.USER);
        CategoryBudgetResponse created = categoryBudgetService.addBudget(category.getId(),
                request("100.00", "2026-06-01", "2026-06-30", null, null));

        CategoryBudgetResponse updated = categoryBudgetService.updateBudget(category.getId(), created.getId(),
                request("125.00", "2026-06-01", "2026-06-30", null, null));

        assertEquals(created.getId(), updated.getId());
        assertEquals(0, new BigDecimal("125.00").compareTo(updated.getBudgetAmount()));
    }

    private User saveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("unused-test-password");
        user.setUuid(UUID.randomUUID());
        return userRepository.saveAndFlush(user);
    }

    private TransactionCategory saveCategory(User owner, String name, String type) {
        setAuthentication(owner.getUuid(), Role.USER);
        TransactionCategory category = new TransactionCategory();
        category.setName(name);
        category.setTransactionType(type);
        return transactionCategoryRepository.saveAndFlush(category);
    }

    private void saveTransaction(Long categoryId, String amount, String currency, String date) {
        Transaction transaction = new Transaction();
        transaction.setName("Test transaction");
        transaction.setTransactionCategoryId(categoryId);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setCurrency(currency);
        transaction.setTransactionDate(LocalDate.parse(date));
        transactionRepository.saveAndFlush(transaction);
    }

    private CategoryBudgetRequest request(String amount, String startDate, String endDate,
                                          BudgetRecurrenceFrequency frequency, Integer customPeriodDays) {
        return CategoryBudgetRequest.builder()
                .budgetAmount(new BigDecimal(amount))
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .recurrenceFrequency(frequency)
                .customPeriodDays(customPeriodDays)
                .build();
    }

    private void setAuthentication(UUID userId, Role role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                Map.of("uuid", userId.toString()),
                null,
                List.of(new SimpleGrantedAuthority(role.getAuthority()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
