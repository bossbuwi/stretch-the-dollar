package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.request.CategoryBudgetRequest;
import com.paradoxdevs.dollar.api.response.CategoryBudgetResponse;
import com.paradoxdevs.dollar.api.response.CategoryBudgetSummaryResponse;
import com.paradoxdevs.dollar.aspect.annotation.CheckOwnership;
import com.paradoxdevs.dollar.entity.BudgetRecurrenceFrequency;
import com.paradoxdevs.dollar.entity.CategoryBudget;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import com.paradoxdevs.dollar.error.ErrorCode;
import com.paradoxdevs.dollar.error.exception.BudgetRuleException;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.repository.CategoryBudgetRepository;
import com.paradoxdevs.dollar.repository.TransactionCategoryRepository;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.service.CategoryBudgetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CategoryBudgetServiceImpl implements CategoryBudgetService {
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryBudgetServiceImpl(CategoryBudgetRepository categoryBudgetRepository,
                                     TransactionCategoryRepository transactionCategoryRepository,
                                     TransactionRepository transactionRepository) {
        this.categoryBudgetRepository = categoryBudgetRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<CategoryBudgetResponse> getBudgets() {
        return categoryBudgetRepository.findAllByOrderByTransactionCategoryIdAscStartDateAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CategoryBudgetResponse> getBudgetsForCategory(Long categoryId) {
        ensureCategoryExists(categoryId);
        return categoryBudgetRepository.findAllByTransactionCategoryIdOrderByStartDateAsc(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryBudgetResponse getBudget(Long budgetId) {
        return categoryBudgetRepository.findById(budgetId)
                .map(this::toResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public CategoryBudgetSummaryResponse getBudgetSummary(Long budgetId, LocalDate asOfDate) {
        CategoryBudget budget = categoryBudgetRepository.findById(budgetId)
                .orElseThrow(ResourceNotFoundException::new);
        LocalDate summaryDate = asOfDate == null ? LocalDate.now() : asOfDate;
        DateRange period = resolvePeriod(budget, summaryDate);
        BigDecimal spent = transactionRepository.sumAmountForCategoryAndDateRange(
                budget.getTransactionCategoryId(), period.startDate(), period.endDate());

        return CategoryBudgetSummaryResponse.builder()
                .budgetId(budget.getId())
                .transactionCategoryId(budget.getTransactionCategoryId())
                .periodStartDate(period.startDate())
                .periodEndDate(period.endDate())
                .budgetAmount(budget.getBudgetAmount())
                .spentAmount(spent)
                .remainingAmount(budget.getBudgetAmount().subtract(spent))
                .build();
    }

    @Transactional
    @CheckOwnership(TransactionCategory.class)
    @Override
    public CategoryBudgetResponse addBudget(Long categoryId, CategoryBudgetRequest request) {
        TransactionCategory category = lockCategory(categoryId);
        validateCategoryAndRequest(category, request);
        ensureNoOverlap(categoryId, request.getStartDate(), request.getEndDate(), null);

        CategoryBudget budget = new CategoryBudget();
        budget.setTransactionCategoryId(categoryId);
        applyRequest(budget, request);
        return toResponse(categoryBudgetRepository.saveAndFlush(budget));
    }

    @Transactional
    @CheckOwnership(TransactionCategory.class)
    @Override
    public CategoryBudgetResponse updateBudget(Long categoryId, Long budgetId, CategoryBudgetRequest request) {
        TransactionCategory category = lockCategory(categoryId);
        CategoryBudget budget = categoryBudgetRepository.findByIdAndTransactionCategoryId(budgetId, categoryId)
                .orElseThrow(ResourceNotFoundException::new);
        validateCategoryAndRequest(category, request);
        ensureNoOverlap(categoryId, request.getStartDate(), request.getEndDate(), budgetId);

        applyRequest(budget, request);
        return toResponse(categoryBudgetRepository.saveAndFlush(budget));
    }

    @Transactional
    @CheckOwnership(TransactionCategory.class)
    @Override
    public CategoryBudgetResponse deleteBudget(Long categoryId, Long budgetId) {
        lockCategory(categoryId);
        CategoryBudget budget = categoryBudgetRepository.findByIdAndTransactionCategoryId(budgetId, categoryId)
                .orElseThrow(ResourceNotFoundException::new);
        CategoryBudgetResponse response = toResponse(budget);
        categoryBudgetRepository.delete(budget);
        return response;
    }

    private TransactionCategory lockCategory(Long categoryId) {
        return transactionCategoryRepository.findByIdForUpdate(categoryId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private void ensureCategoryExists(Long categoryId) {
        if (!transactionCategoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException();
        }
    }

    private void validateCategoryAndRequest(TransactionCategory category, CategoryBudgetRequest request) {
        if (!"EXPENSE".equalsIgnoreCase(category.getTransactionType())) {
            throw new BudgetRuleException(ErrorCode.REQUEST_VALIDATION_ERROR,
                    "Budgets can only be created for expense categories.");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BudgetRuleException(ErrorCode.REQUEST_VALIDATION_ERROR,
                    "Budget start date must be on or before its end date.");
        }

        BudgetRecurrenceFrequency frequency = request.getRecurrenceFrequency();
        Integer customPeriodDays = request.getCustomPeriodDays();
        if (frequency == BudgetRecurrenceFrequency.CUSTOM_DAYS) {
            if (customPeriodDays == null || customPeriodDays < 1) {
                throw new BudgetRuleException(ErrorCode.REQUEST_VALIDATION_ERROR,
                        "A custom recurrence requires a period of at least one day.");
            }
        } else if (customPeriodDays != null) {
            throw new BudgetRuleException(ErrorCode.REQUEST_VALIDATION_ERROR,
                    "Custom period days are only valid with CUSTOM_DAYS recurrence.");
        }
    }

    private void ensureNoOverlap(Long categoryId, LocalDate startDate, LocalDate endDate, Long excludedId) {
        boolean overlaps = excludedId == null
                ? categoryBudgetRepository.existsByTransactionCategoryIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        categoryId, endDate, startDate)
                : categoryBudgetRepository
                        .existsByTransactionCategoryIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
                                categoryId, endDate, startDate, excludedId);
        if (overlaps) {
            throw new BudgetRuleException(ErrorCode.BUDGET_PERIOD_OVERLAP,
                    "This budget period overlaps another budget for the same category.");
        }
    }

    private void applyRequest(CategoryBudget budget, CategoryBudgetRequest request) {
        budget.setBudgetAmount(request.getBudgetAmount());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setRecurrenceFrequency(request.getRecurrenceFrequency());
        budget.setCustomPeriodDays(request.getCustomPeriodDays());
    }

    private CategoryBudgetResponse toResponse(CategoryBudget budget) {
        return CategoryBudgetResponse.builder()
                .id(budget.getId())
                .transactionCategoryId(budget.getTransactionCategoryId())
                .budgetAmount(budget.getBudgetAmount())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .recurrenceFrequency(budget.getRecurrenceFrequency())
                .customPeriodDays(budget.getCustomPeriodDays())
                .build();
    }

    private DateRange resolvePeriod(CategoryBudget budget, LocalDate asOfDate) {
        if (budget.getRecurrenceFrequency() == null) {
            return new DateRange(budget.getStartDate(), budget.getEndDate());
        }
        if (asOfDate.isBefore(budget.getStartDate()) || asOfDate.isAfter(budget.getEndDate())) {
            throw new BudgetRuleException(ErrorCode.REQUEST_VALIDATION_ERROR,
                    "The selected date must fall within the recurring budget schedule.");
        }

        return switch (budget.getRecurrenceFrequency()) {
            case WEEKLY -> fixedDayPeriod(budget, asOfDate, 7);
            case CUSTOM_DAYS -> fixedDayPeriod(budget, asOfDate, budget.getCustomPeriodDays());
            case MONTHLY -> calendarPeriod(budget, asOfDate, false);
            case YEARLY -> calendarPeriod(budget, asOfDate, true);
        };
    }

    private DateRange fixedDayPeriod(CategoryBudget budget, LocalDate asOfDate, int periodDays) {
        long daysSinceStart = ChronoUnit.DAYS.between(budget.getStartDate(), asOfDate);
        LocalDate periodStart = budget.getStartDate().plusDays((daysSinceStart / periodDays) * periodDays);
        LocalDate periodEnd = safePlusDays(periodStart, periodDays - 1L);
        return new DateRange(periodStart, minDate(periodEnd, budget.getEndDate()));
    }

    private DateRange calendarPeriod(CategoryBudget budget, LocalDate asOfDate, boolean yearly) {
        LocalDate scheduleStart = budget.getStartDate();
        long periodIndex = yearly
                ? asOfDate.getYear() - scheduleStart.getYear()
                : ChronoUnit.MONTHS.between(YearMonth.from(scheduleStart), YearMonth.from(asOfDate));
        LocalDate periodStart = calendarBoundary(scheduleStart, periodIndex, yearly);
        if (asOfDate.isBefore(periodStart)) {
            periodIndex--;
            periodStart = calendarBoundary(scheduleStart, periodIndex, yearly);
        }

        LocalDate nextStart;
        try {
            nextStart = calendarBoundary(scheduleStart, periodIndex + 1, yearly);
        } catch (DateTimeException exception) {
            return new DateRange(periodStart, budget.getEndDate());
        }
        LocalDate periodEnd = nextStart.minusDays(1);
        return new DateRange(periodStart, minDate(periodEnd, budget.getEndDate()));
    }

    private LocalDate calendarBoundary(LocalDate startDate, long periodIndex, boolean yearly) {
        return yearly ? startDate.plusYears(periodIndex) : startDate.plusMonths(periodIndex);
    }

    private LocalDate safePlusDays(LocalDate date, long days) {
        try {
            return date.plusDays(days);
        } catch (DateTimeException exception) {
            return LocalDate.MAX;
        }
    }

    private LocalDate minDate(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {}
}
