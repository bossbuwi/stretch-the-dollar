package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.paradoxdevs.dollar.exception.ErrorCode.RESOURCE_NOT_FOUND;
import static com.paradoxdevs.dollar.helper.TransactionDataHelper.createValidTransaction;
import static com.paradoxdevs.dollar.helper.TransactionDataHelper.createValidTransactionRequest;
import static com.paradoxdevs.dollar.helper.TransactionDataHelper.createValidTransactionResponse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Nested
    @DisplayName("Tests for getTransactions()")
    class GetTransactionsTests {

        @Test
        @DisplayName("Database is not empty")
        void shouldReturnAllTransactions() {
            Transaction t1 = new Transaction();
            t1.setId(1L);
            TransactionResponse r1 = new TransactionResponse();
            Transaction t2 = new Transaction();
            t2.setId(2L);
            TransactionResponse r2 = new TransactionResponse();

            when(transactionRepository.findAll()).thenReturn(List.of(t1, t2));
            when(transactionMapper.entityToResponse(t1)).thenReturn(r1);
            when(transactionMapper.entityToResponse(t2)).thenReturn(r2);

            List<TransactionResponse> result = transactionService.getTransactions();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertSame(r1, result.get(0));
            assertSame(r2, result.get(1));

            verify(transactionRepository, times(1)).findAll();
            verify(transactionMapper, times(1)).entityToResponse(t1);
            verify(transactionMapper, times(1)).entityToResponse(t2);
        }

        @Test
        @DisplayName("Database is empty")
        void shouldReturnEmptyListWhenNoTransactionsFound() {
            when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

            List<TransactionResponse> result = transactionService.getTransactions();

            assertNotNull(result);
            assertEquals(0, result.size());

            verify(transactionRepository, times(1)).findAll();
            verifyNoInteractions(transactionMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            when(transactionRepository.findAll()).thenThrow(databaseException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactions());

            assertEquals("Database connection timed out", exception.getMessage());

            verify(transactionRepository, times(1)).findAll();
            verifyNoInteractions(transactionMapper);
        }
    }

    @Nested
    @DisplayName("Tests for getTransactionById()")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("Transaction with id exists")
        void shouldReturnTransactionById() {
            Transaction t1 = new Transaction();
            t1.setId(1L);
            TransactionResponse r1 = new TransactionResponse();

            when(transactionRepository.findById(1L)).thenReturn(Optional.of(t1));
            when(transactionMapper.entityToResponse(t1)).thenReturn(r1);

            TransactionResponse result = transactionService.getTransactionById(1L);

            assertSame(r1, result);
            verify(transactionRepository, times(1)).findById(1L);
            verify(transactionMapper, times(1)).entityToResponse(t1);
        }

        @Test
        @DisplayName("Transaction with id does not exist")
        void shouldReturnExceptionWhenTransactionNotFound() {
            when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactionById(1L));

            assertInstanceOf(ResourceNotFoundException.class, exception);
            assertEquals(RESOURCE_NOT_FOUND.getErrorMessage(), exception.getMessage());
            verify(transactionRepository, times(1)).findById(1L);
            verifyNoInteractions(transactionMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            when(transactionRepository.findById(1L)).thenThrow(databaseException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactionById(1L));

            assertEquals("Database connection timed out", exception.getMessage());

            verify(transactionRepository, times(1)).findById(1L);
            verifyNoInteractions(transactionMapper);
        }
    }

    @Nested
    @DisplayName("Tests for addTransaction()")
    class AddTransactionTests {

        @Test
        @DisplayName("Save valid transaction")
        void shouldSaveTransaction() {
            TransactionRequest req = createValidTransactionRequest();
            TransactionResponse res = createValidTransactionResponse();
            Transaction t1 = createValidTransaction();

            when(transactionMapper.requestToEntity(req)).thenReturn(t1);
            when(transactionRepository.save(t1)).thenReturn(t1);
            when(transactionMapper.entityToResponse(t1)).thenReturn(res);

            TransactionResponse result = transactionService.addTransaction(req);

            assertSame(res, result);
            verify(transactionRepository, times(1)).save(t1);
            verify(transactionMapper, times(1)).requestToEntity(req);
            verify(transactionMapper, times(1)).entityToResponse(t1);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            when(transactionRepository.save(any())).thenThrow(databaseException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.addTransaction(any()));

            assertEquals("Database connection timed out", exception.getMessage());

            verify(transactionRepository, times(1)).save(any());
            verify(transactionMapper, times(1)).requestToEntity(any());
            verifyNoMoreInteractions(transactionMapper);
        }
    }

    @Nested
    @DisplayName("Tests for updateTransaction()")
    class UpdateTransactionTests {

        @Test
        @DisplayName("Update valid transaction")
        void shouldUpdateTransaction() {
            long updateId = 1L;
            TransactionRequest req = createValidTransactionRequest();
        }
    }

    @Nested
    @DisplayName("Tests for deleteTransaction()")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Delete transaction when available")
        void shouldDeleteTransaction() {
            long deleteId = 1L;

            assertDoesNotThrow(() -> transactionService.deleteTransaction(deleteId));
            verify(transactionRepository, times(1)).deleteById(deleteId);
            verifyNoInteractions(transactionMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            long deleteId = 1L;
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            doThrow(databaseException).when(transactionRepository).deleteById(deleteId);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.deleteTransaction(deleteId));

            assertEquals("Database connection timed out", exception.getMessage());

            verify(transactionRepository, times(1)).deleteById(deleteId);
            verifyNoInteractions(transactionMapper);
        }
    }
}
