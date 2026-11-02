package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.TransactionCategoryResponse;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionCategoryMapper;
import com.paradoxdevs.dollar.repository.TransactionCategoryRepository;
import com.paradoxdevs.dollar.service.impl.TransactionCategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCategoryServiceTest {
    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;
    @Spy
    private TransactionCategoryMapper transactionCategoryMapper = Mappers.getMapper(TransactionCategoryMapper.class);
    @InjectMocks
    private TransactionCategoryServiceImpl transactionCategoryService;

    @Test
    @DisplayName("Get transaction items maps all categories in repository order")
    void getTransactionItems_mapsAllCategoriesInRepositoryOrder() {
        TransactionCategory groceries = category(1L, "Groceries", "EXPENSE");
        TransactionCategory salary = category(2L, "Salary", "INCOME");
        when(transactionCategoryRepository.findAll()).thenReturn(List.of(groceries, salary));

        List<TransactionCategoryResponse> response = transactionCategoryService.getTransactionItems();

        assertEquals(2, response.size());
        assertEquals("Groceries", response.get(0).getName());
        assertEquals("EXPENSE", response.get(0).getTransactionType());
        assertEquals("Salary", response.get(1).getName());
        assertEquals("INCOME", response.get(1).getTransactionType());
        verify(transactionCategoryRepository).findAll();
        verify(transactionCategoryMapper).entityToResponse(groceries);
        verify(transactionCategoryMapper).entityToResponse(salary);
        verifyNoMoreInteractions(transactionCategoryRepository);
    }

    @Test
    @DisplayName("Get transaction items returns empty list when no categories exist")
    void getTransactionItems_returnsEmptyListWhenNoCategoriesExist() {
        when(transactionCategoryRepository.findAll()).thenReturn(List.of());

        List<TransactionCategoryResponse> response = transactionCategoryService.getTransactionItems();

        assertTrue(response.isEmpty());
        verify(transactionCategoryRepository).findAll();
        verifyNoInteractions(transactionCategoryMapper);
    }

    @Test
    @DisplayName("Get transaction items propagates repository failure")
    void getTransactionItems_propagatesRepositoryFailure() {
        RuntimeException failure = new RuntimeException("Database unavailable");
        when(transactionCategoryRepository.findAll()).thenThrow(failure);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionCategoryService.getTransactionItems());

        assertEquals(failure, exception);
        verifyNoInteractions(transactionCategoryMapper);
    }

    @Test
    @DisplayName("Get transaction item by id returns mapped category")
    void getTransactionItemById_returnsMappedCategory() {
        TransactionCategory category = category(8L, "Utilities", "EXPENSE");
        category.setCreatedBy(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        category.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
        category.setUpdatedBy(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        category.setUpdatedAt(Instant.parse("2026-02-01T10:00:00Z"));
        when(transactionCategoryRepository.findById(8L)).thenReturn(Optional.of(category));

        TransactionCategoryResponse response = transactionCategoryService.getTransactionItemById(8L);

        assertEquals(8L, response.getId());
        assertEquals("Utilities", response.getName());
        assertEquals("Recurring bill", response.getDescription());
        assertEquals("EXPENSE", response.getTransactionType());
        assertEquals("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", response.getCreatedBy());
        assertEquals("2026-01-01T10:00:00Z", response.getCreatedAt());
        assertEquals("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", response.getUpdatedBy());
        assertEquals("2026-02-01T10:00:00Z", response.getUpdatedAt());
        verify(transactionCategoryRepository).findById(8L);
        verify(transactionCategoryMapper).entityToResponse(category);
        verifyNoMoreInteractions(transactionCategoryRepository);
    }

    @Test
    @DisplayName("Get transaction item by id throws when category does not exist")
    void getTransactionItemById_throwsWhenCategoryDoesNotExist() {
        when(transactionCategoryRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionCategoryService.getTransactionItemById(8L));

        verify(transactionCategoryRepository).findById(8L);
        verifyNoInteractions(transactionCategoryMapper);
    }

    @Test
    @DisplayName("Add transaction item maps request saves entity and returns saved category")
    void addTransactionItem_mapsRequestSavesEntityAndReturnsSavedCategory() {
        TransactionCategoryRequest request = TransactionCategoryRequest.builder()
                .name("Travel")
                .description("Trip expenses")
                .transactionType("EXPENSE")
                .build();
        TransactionCategory saved = category(15L, "Travel", "EXPENSE");
        saved.setDescription("Trip expenses");
        when(transactionCategoryRepository.save(any(TransactionCategory.class))).thenReturn(saved);

        TransactionCategoryResponse response = transactionCategoryService.addTransactionItem(request);

        ArgumentCaptor<TransactionCategory> entityCaptor = ArgumentCaptor.forClass(TransactionCategory.class);
        verify(transactionCategoryRepository).save(entityCaptor.capture());
        TransactionCategory input = entityCaptor.getValue();
        assertEquals("Travel", input.getName());
        assertEquals("Trip expenses", input.getDescription());
        assertEquals("EXPENSE", input.getTransactionType());
        assertNull(input.getId());
        assertEquals(15L, response.getId());
        assertEquals("Travel", response.getName());
        assertEquals("Trip expenses", response.getDescription());
        assertEquals("EXPENSE", response.getTransactionType());
        verify(transactionCategoryMapper).requestToEntity(request);
        verify(transactionCategoryMapper).entityToResponse(saved);
    }

    @Test
    @DisplayName("Add transaction item propagates repository failure")
    void addTransactionItem_propagatesRepositoryFailure() {
        TransactionCategoryRequest request = TransactionCategoryRequest.builder()
                .name("Travel")
                .description("Trip expenses")
                .transactionType("EXPENSE")
                .build();
        RuntimeException failure = new RuntimeException("Database unavailable");
        when(transactionCategoryRepository.save(any(TransactionCategory.class))).thenThrow(failure);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionCategoryService.addTransactionItem(request));

        assertEquals(failure, exception);
        verify(transactionCategoryMapper).requestToEntity(request);
        verifyNoMoreInteractions(transactionCategoryMapper);
    }

    @Test
    @DisplayName("Update transaction item changes editable fields and preserves id and owner")
    void updateTransactionItem_preservesIdentityAndOwner() {
        TransactionCategory category = category(15L, "Old name", "EXPENSE");
        UUID owner = UUID.randomUUID();
        category.setCreatedBy(owner);
        TransactionCategoryRequest request = TransactionCategoryRequest.builder()
                .name("New name")
                .description("Updated description")
                .transactionType("INCOME")
                .build();
        when(transactionCategoryRepository.findById(15L)).thenReturn(Optional.of(category));
        when(transactionCategoryRepository.saveAndFlush(category)).thenReturn(category);

        TransactionCategoryResponse response = transactionCategoryService.updateTransactionItem(15L, request);

        assertEquals(15L, category.getId());
        assertEquals(owner, category.getCreatedBy());
        assertEquals("New name", category.getName());
        assertEquals("Updated description", category.getDescription());
        assertEquals("INCOME", category.getTransactionType());
        assertEquals(15L, response.getId());
        assertEquals("New name", response.getName());
        verify(transactionCategoryRepository).findById(15L);
        verify(transactionCategoryRepository).saveAndFlush(category);
        verify(transactionCategoryMapper).updateEntityFromRequest(request, category);
        verify(transactionCategoryMapper).entityToResponse(category);
    }

    @Test
    @DisplayName("Update transaction item throws when category does not exist")
    void updateTransactionItem_throwsWhenCategoryDoesNotExist() {
        TransactionCategoryRequest request = TransactionCategoryRequest.builder()
                .name("New name")
                .transactionType("EXPENSE")
                .build();
        when(transactionCategoryRepository.findById(15L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionCategoryService.updateTransactionItem(15L, request));

        verify(transactionCategoryRepository).findById(15L);
        verifyNoMoreInteractions(transactionCategoryRepository);
        verifyNoInteractions(transactionCategoryMapper);
    }

    private TransactionCategory category(Long id, String name, String transactionType) {
        TransactionCategory category = new TransactionCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(name.equals("Utilities") ? "Recurring bill" : null);
        category.setTransactionType(transactionType);
        return category;
    }
}
