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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void updateTransaction_throwsWhenTransactionDoesNotExist() {
        long id = 12L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.updateTransaction(id, new TransactionRequest()));

        verifyNoInteractions(transactionDetailsView, transactionMapper);
    }

    @Test
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
    void deleteTransaction_throwsWhenTransactionDoesNotExist() {
        long id = 12L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(id));

        verifyNoInteractions(transactionDetailsView, transactionMapper);
    }

    @Test
    void deleteTransaction_throwsWhenDetailsViewDoesNotContainTransaction() {
        long id = 12L;
        Transaction transaction = new Transaction();
        transaction.setId(id);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(transactionDetailsView.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(id));

        verifyNoInteractions(transactionMapper);
    }
}
