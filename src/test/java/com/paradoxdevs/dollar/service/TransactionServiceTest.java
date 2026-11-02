package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionDetailsResponse;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.entity.view.TransactionDetails;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.repository.view.TransactionDetailsView;
import com.paradoxdevs.dollar.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionDetailsView transactionDetailsView;
    @Spy
    private TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("Get transactions maps all details and preserves order")
    void getTransactions_mapsAllDetailsAndPreservesOrder() {
        TransactionDetails first = transactionDetails(1L, "First transaction");
        TransactionDetails second = transactionDetails(2L, "Second transaction");
        when(transactionDetailsView.findAll()).thenReturn(List.of(first, second));

        List<TransactionDetailsResponse> response = transactionService.getTransactions();

        assertEquals(2, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals("First transaction", response.get(0).getName());
        assertEquals(2L, response.get(1).getId());
        assertEquals("Second transaction", response.get(1).getName());
        verify(transactionDetailsView).findAll();
        verify(transactionMapper).viewToResponse(first);
        verify(transactionMapper).viewToResponse(second);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Get transactions returns empty list when no transactions exist")
    void getTransactions_returnsEmptyListWhenNoTransactionsExist() {
        when(transactionDetailsView.findAll()).thenReturn(List.of());

        List<TransactionDetailsResponse> response = transactionService.getTransactions();

        assertTrue(response.isEmpty());
        verify(transactionDetailsView).findAll();
        verifyNoInteractions(transactionRepository, transactionMapper);
    }

    @Test
    @DisplayName("Get transaction by id returns mapped details")
    void getTransactionById_returnsMappedDetails() {
        TransactionDetails details = transactionDetails(12L, "Requested transaction");
        when(transactionDetailsView.findById(12L)).thenReturn(Optional.of(details));

        TransactionDetailsResponse response = transactionService.getTransactionById(12L);

        assertEquals(12L, response.getId());
        assertEquals("Requested transaction", response.getName());
        assertEquals("2026-02-20", response.getTransactionDate());
        assertEquals(new BigDecimal("25.50"), response.getAmount());
        assertEquals("Groceries", response.getTransactionCategoryName());
        assertEquals("EXPENSE", response.getTransactionType());
        assertEquals("creator", response.getCreatedBy());
        assertEquals("2026-01-01T10:00:00Z", response.getCreatedAt());
        assertEquals("editor", response.getUpdatedBy());
        assertEquals("2026-02-01T10:00:00Z", response.getUpdatedAt());
        verify(transactionDetailsView).findById(12L);
        verify(transactionMapper).viewToResponse(details);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Get transaction by id throws when transaction does not exist")
    void getTransactionById_throwsWhenTransactionDoesNotExist() {
        when(transactionDetailsView.findById(12L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(12L));

        verify(transactionDetailsView).findById(12L);
        verifyNoInteractions(transactionRepository, transactionMapper);
    }

    @Test
    @DisplayName("Add transaction saves mapped entity and returns details")
    void addTransaction_savesMappedEntityAndReturnsDetails() {
        TransactionRequest request = validRequest("2026-02-20");
        Transaction saved = new Transaction();
        saved.setId(12L);
        TransactionDetails details = transactionDetails(12L, "New transaction");
        when(transactionRepository.save(org.mockito.ArgumentMatchers.any(Transaction.class))).thenReturn(saved);
        when(transactionDetailsView.findById(12L)).thenReturn(Optional.of(details));

        TransactionDetailsResponse response = transactionService.addTransaction(request);

        ArgumentCaptor<Transaction> savedTransaction = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(savedTransaction.capture());
        assertEquals("New transaction", savedTransaction.getValue().getName());
        assertEquals("Description", savedTransaction.getValue().getDescription());
        assertEquals(4L, savedTransaction.getValue().getTransactionCategoryId());
        assertEquals(LocalDate.parse("2026-02-20"), savedTransaction.getValue().getTransactionDate());
        assertEquals(new BigDecimal("25.50"), savedTransaction.getValue().getAmount());
        assertEquals("USD", savedTransaction.getValue().getCurrency());
        assertEquals(12L, response.getId());
        assertEquals("New transaction", response.getName());
        assertEquals("2026-02-20", response.getTransactionDate());
        assertEquals("creator", response.getCreatedBy());
        assertEquals("editor", response.getUpdatedBy());
        assertNull(savedTransaction.getValue().getId());
        assertNull(savedTransaction.getValue().getCreatedBy());
        assertNull(savedTransaction.getValue().getCreatedAt());
        assertNull(savedTransaction.getValue().getUpdatedBy());
        assertNull(savedTransaction.getValue().getUpdatedAt());
        verify(transactionDetailsView).findById(12L);
    }

    @Test
    @DisplayName("Add transaction throws when saved transaction is missing from details view")
    void addTransaction_throwsWhenSavedTransactionIsMissingFromDetailsView() {
        Transaction saved = new Transaction();
        saved.setId(12L);
        when(transactionRepository.save(org.mockito.ArgumentMatchers.any(Transaction.class))).thenReturn(saved);
        when(transactionDetailsView.findById(12L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.addTransaction(validRequest("2026-02-20")));

        verify(transactionRepository).save(org.mockito.ArgumentMatchers.any(Transaction.class));
        verify(transactionDetailsView).findById(12L);
    }

    @Test
    @DisplayName("Add transaction rejects malformed date before saving")
    void addTransaction_rejectsMalformedDateBeforeSaving() {
        assertThrows(DateTimeParseException.class,
                () -> transactionService.addTransaction(validRequest("20-02-2026")));

        verifyNoInteractions(transactionRepository, transactionDetailsView);
    }

    @Test
    @DisplayName("Update transaction updates editable fields and returns refreshed details")
    void updateTransaction_updatesEditableFieldsAndReturnsRefreshedDetails() {
        long id = 12L;
        UUID creator = UUID.randomUUID();
        UUID previousEditor = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant previousUpdatedAt = Instant.parse("2026-01-02T10:00:00Z");
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setName("Old name");
        transaction.setCreatedBy(creator);
        transaction.setCreatedAt(createdAt);
        transaction.setUpdatedBy(previousEditor);
        transaction.setUpdatedAt(previousUpdatedAt);

        TransactionRequest request = TransactionRequest.builder()
                .name("Updated name")
                .description("Updated description")
                .transactionCategoryId(4L)
                .transactionDate("2026-02-20")
                .amount(new BigDecimal("25.50"))
                .currency("USD")
                .build();
        TransactionDetails details = new TransactionDetails(
                id,
                "Updated name",
                "Updated description",
                "USD",
                new BigDecimal("25.50"),
                LocalDate.parse("2026-02-20"),
                "Groceries",
                "EXPENSE",
                "creator",
                createdAt,
                "editor",
                Instant.parse("2026-02-01T10:00:00Z")
        );
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(transactionRepository.saveAndFlush(transaction)).thenReturn(transaction);
        when(transactionDetailsView.findById(id)).thenReturn(Optional.of(details));

        TransactionDetailsResponse response = transactionService.updateTransaction(id, request);

        assertEquals("Updated name", transaction.getName());
        assertEquals("Updated description", transaction.getDescription());
        assertEquals(4L, transaction.getTransactionCategoryId());
        assertEquals(LocalDate.parse("2026-02-20"), transaction.getTransactionDate());
        assertEquals(new BigDecimal("25.50"), transaction.getAmount());
        assertEquals("USD", transaction.getCurrency());
        assertEquals(creator, transaction.getCreatedBy());
        assertEquals(createdAt, transaction.getCreatedAt());
        assertEquals(previousEditor, transaction.getUpdatedBy());
        assertEquals(previousUpdatedAt, transaction.getUpdatedAt());
        assertEquals("editor", response.getUpdatedBy());
        assertEquals("2026-02-01T10:00:00Z", response.getUpdatedAt());
        verify(transactionRepository).saveAndFlush(transaction);
        verify(transactionDetailsView).findById(id);
    }

    @Test
    @DisplayName("Update transaction throws when transaction does not exist")
    void updateTransaction_throwsWhenTransactionDoesNotExist() {
        long id = 12L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.updateTransaction(id, new TransactionRequest()));

        verifyNoInteractions(transactionDetailsView, transactionMapper);
    }

    @Test
    @DisplayName("Update transaction rejects malformed date before saving")
    void updateTransaction_rejectsMalformedDateBeforeSaving() {
        Transaction transaction = new Transaction();
        transaction.setId(12L);
        when(transactionRepository.findById(12L)).thenReturn(Optional.of(transaction));

        assertThrows(DateTimeParseException.class,
                () -> transactionService.updateTransaction(12L, validRequest("20-02-2026")));

        verify(transactionRepository).findById(12L);
        verifyNoMoreInteractions(transactionRepository);
        verifyNoInteractions(transactionDetailsView);
    }

    @Test
    @DisplayName("Update transaction does not read response view when save fails")
    void updateTransaction_doesNotReadResponseViewWhenSaveFails() {
        Transaction transaction = new Transaction();
        transaction.setId(12L);
        RuntimeException persistenceFailure = new RuntimeException("Database unavailable");
        when(transactionRepository.findById(12L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.saveAndFlush(transaction)).thenThrow(persistenceFailure);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.updateTransaction(12L, validRequest("2026-02-20")));

        assertEquals(persistenceFailure, exception);
        verify(transactionRepository).findById(12L);
        verify(transactionRepository).saveAndFlush(transaction);
        verifyNoInteractions(transactionDetailsView);
    }

    @Test
    @DisplayName("Update transaction throws when updated entity is missing from details view")
    void updateTransaction_throwsWhenUpdatedEntityIsMissingFromDetailsView() {
        Transaction transaction = new Transaction();
        transaction.setId(12L);
        when(transactionRepository.findById(12L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.saveAndFlush(transaction)).thenReturn(transaction);
        when(transactionDetailsView.findById(12L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(12L, validRequest("2026-02-20")));

        verify(transactionRepository).saveAndFlush(transaction);
        verify(transactionDetailsView).findById(12L);
    }

    @Test
    @DisplayName("Delete transaction returns details before deleting")
    void deleteTransaction_returnsDetailsBeforeDeleting() {
        long id = 12L;
        Transaction transaction = new Transaction();
        transaction.setId(id);
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        TransactionDetails details = new TransactionDetails(
                id,
                "Deleted transaction",
                "Receipt",
                "USD",
                new BigDecimal("25.50"),
                LocalDate.parse("2026-02-20"),
                "Groceries",
                "EXPENSE",
                "creator",
                createdAt,
                "editor",
                Instant.parse("2026-02-21T10:00:00Z")
        );
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(transactionDetailsView.findById(id)).thenReturn(Optional.of(details));

        TransactionDetailsResponse response = transactionService.deleteTransaction(id);

        assertEquals(id, response.getId());
        assertEquals("Deleted transaction", response.getName());
        assertEquals("creator", response.getCreatedBy());
        assertEquals("editor", response.getUpdatedBy());
        var order = inOrder(transactionDetailsView, transactionRepository);
        order.verify(transactionDetailsView).findById(id);
        order.verify(transactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("Delete transaction throws when transaction does not exist")
    void deleteTransaction_throwsWhenTransactionDoesNotExist() {
        long id = 12L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(id));

        verifyNoInteractions(transactionDetailsView, transactionMapper);
    }

    @Test
    @DisplayName("Delete transaction throws when details view does not contain transaction")
    void deleteTransaction_throwsWhenDetailsViewDoesNotContainTransaction() {
        long id = 12L;
        Transaction transaction = new Transaction();
        transaction.setId(id);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(transactionDetailsView.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(id));

        verify(transactionRepository).findById(id);
        verify(transactionDetailsView).findById(id);
        verifyNoMoreInteractions(transactionRepository, transactionDetailsView);
        verifyNoInteractions(transactionMapper);
    }

    private TransactionDetails transactionDetails(Long id, String name) {
        return new TransactionDetails(
                id,
                name,
                "Description",
                "USD",
                new BigDecimal("25.50"),
                LocalDate.parse("2026-02-20"),
                "Groceries",
                "EXPENSE",
                "creator",
                Instant.parse("2026-01-01T10:00:00Z"),
                "editor",
                Instant.parse("2026-02-01T10:00:00Z")
        );
    }

    private TransactionRequest validRequest(String date) {
        return TransactionRequest.builder()
                .name("New transaction")
                .description("Description")
                .transactionCategoryId(4L)
                .transactionDate(date)
                .amount(new BigDecimal("25.50"))
                .currency("USD")
                .build();
    }
}
