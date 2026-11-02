package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.TransactionCategoryResponse;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionCategoryMapper;
import com.paradoxdevs.dollar.repository.TransactionCategoryRepository;
import com.paradoxdevs.dollar.service.impl.TransactionCategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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
    void getTransactionItems_returnsEmptyListWhenNoCategoriesExist() {
        when(transactionCategoryRepository.findAll()).thenReturn(List.of());

        List<TransactionCategoryResponse> response = transactionCategoryService.getTransactionItems();

        assertTrue(response.isEmpty());
        verify(transactionCategoryRepository).findAll();
        verifyNoInteractions(transactionCategoryMapper);
    }

    @Test
    void getTransactionItems_propagatesRepositoryFailure() {
        RuntimeException failure = new RuntimeException("Database unavailable");
        when(transactionCategoryRepository.findAll()).thenThrow(failure);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionCategoryService.getTransactionItems());

        assertEquals(failure, exception);
        verifyNoInteractions(transactionCategoryMapper);
    }

    @Test
    void getTransactionItemById_returnsMappedCategory() {
        TransactionCategory category = category(8L, "Utilities", "EXPENSE");
        when(transactionCategoryRepository.findById(8L)).thenReturn(Optional.of(category));

        TransactionCategoryResponse response = transactionCategoryService.getTransactionItemById(8L);

        assertEquals(8L, response.getId());
        assertEquals("Utilities", response.getName());
        assertEquals("Recurring bill", response.getDescription());
        assertEquals("EXPENSE", response.getTransactionType());
        verify(transactionCategoryRepository).findById(8L);
        verify(transactionCategoryMapper).entityToResponse(category);
        verifyNoMoreInteractions(transactionCategoryRepository);
    }

    @Test
    void getTransactionItemById_throwsWhenCategoryDoesNotExist() {
        when(transactionCategoryRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionCategoryService.getTransactionItemById(8L));

        verify(transactionCategoryRepository).findById(8L);
        verifyNoInteractions(transactionCategoryMapper);
    }

    @Test
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

    private TransactionCategory category(Long id, String name, String transactionType) {
        TransactionCategory category = new TransactionCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(name.equals("Utilities") ? "Recurring bill" : null);
        category.setTransactionType(transactionType);
        return category;
    }
}
