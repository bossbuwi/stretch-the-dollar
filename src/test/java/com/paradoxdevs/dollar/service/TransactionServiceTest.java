package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.entity.TransactionWithUsername;
import com.paradoxdevs.dollar.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.repository.TransactionWithUsernameRepository;
import com.paradoxdevs.dollar.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.paradoxdevs.dollar.exception.ErrorCode.RESOURCE_NOT_FOUND;
import static com.paradoxdevs.dollar.helper.TransactionDataHelper.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private TransactionWithUsernameRepository transactionWithUsernameRepository;
    @Spy
    private TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Nested
    @DisplayName("Tests for getTransactions()")
    class GetTransactionsTests {

        @Test
        @DisplayName("Database is not empty.")
        void shouldReturnAllTransactions() {
            TransactionWithUsername t1 = createValidTransactionWithUsername();
            TransactionWithUsername t2 = createValidTransactionWithUsername();
            long t2Id = 2L;
            t2.setId(t2Id);

            when(transactionWithUsernameRepository.findAll()).thenReturn(List.of(t1, t2));

            List<TransactionResponse> result = transactionService.getTransactions();

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(transactionWithUsernameRepository, times(1)).findAll();
            verify(transactionMapper, times(1)).entityWithUsernameToResponse(t1);
            verify(transactionMapper, times(1)).entityWithUsernameToResponse(t2);
        }

        @Test
        @DisplayName("Database is empty.")
        void shouldReturnEmptyListWhenNoTransactionsFound() {
            when(transactionWithUsernameRepository.findAll()).thenReturn(Collections.emptyList());

            List<TransactionResponse> result = transactionService.getTransactions();

            assertNotNull(result);
            assertEquals(0, result.size());
            verify(transactionWithUsernameRepository, times(1)).findAll();
            verifyNoInteractions(transactionMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails.")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            when(transactionWithUsernameRepository.findAll()).thenThrow(databaseException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactions());

            assertEquals("Database connection timed out", exception.getMessage());
            verify(transactionWithUsernameRepository, times(1)).findAll();
            verifyNoInteractions(transactionMapper);
        }
    }

    @Nested
    @DisplayName("Tests for getTransactionById()")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("Transaction with id exists.")
        void shouldReturnTransactionById() {
            TransactionWithUsername t1 = createValidTransactionWithUsername();

            when(transactionWithUsernameRepository.findById(1L)).thenReturn(Optional.of(t1));

            TransactionResponse result = transactionService.getTransactionById(1L);

            assertNotNull(result);
            assertEquals(ID, result.getTransactionId());
            verify(transactionWithUsernameRepository, times(1)).findById(1L);
            verify(transactionMapper, times(1)).entityWithUsernameToResponse(t1);
        }

        @Test
        @DisplayName("Transaction with id does not exist.")
        void shouldReturnExceptionWhenTransactionNotFound() {
            when(transactionWithUsernameRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactionById(1L));

            assertInstanceOf(ResourceNotFoundException.class, exception);
            assertEquals(RESOURCE_NOT_FOUND.getErrorMessage(), exception.getMessage());
            verify(transactionWithUsernameRepository , times(1)).findById(1L);
            verifyNoInteractions(transactionMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails.")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            when(transactionWithUsernameRepository.findById(1L)).thenThrow(databaseException);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionService.getTransactionById(1L));

            assertEquals("Database connection timed out", exception.getMessage());
            verify(transactionWithUsernameRepository, times(1)).findById(1L);
            verifyNoInteractions(transactionMapper);
        }
    }

    @Nested
    @DisplayName("Tests for addTransaction()")
    class AddTransactionTests {

        @Test
        @DisplayName("Save valid transaction.")
        void shouldSaveTransaction() {
            TransactionRequest req = createValidTransactionRequest();
            Transaction in = createValidTransaction();
            in.setId(null);
            Transaction saved = createValidTransaction();
            TransactionWithUsername out =  createValidTransactionWithUsername();

            when(transactionRepository.save(in)).thenReturn(saved);
            when(transactionWithUsernameRepository.findById(saved.getId())).thenReturn(Optional.of(out));

            TransactionResponse result = transactionService.addTransaction(req);

            assertNotNull(result);
            assertEquals(ID, result.getTransactionId());
            verify(transactionRepository, times(1)).save(in);
            verify(transactionMapper, times(1)).requestToEntity(req);
            verify(transactionMapper, times(1)).entityWithUsernameToResponse(out);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails.")
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
        @DisplayName("Update valid transaction.")
        void shouldUpdateTransaction() {
            long updateId = 1L;
            TransactionRequest req = createValidTransactionRequest();
            String newDescription = "new description";
            req.setDescription(newDescription);

            Transaction t1 = createValidTransaction();
            Transaction t2 = createValidTransaction();
            t2.setDescription(newDescription);
            TransactionWithUsername out =  createValidTransactionWithUsername();
            out.setId(updateId);
            out.setDescription(newDescription);

            when(transactionRepository.findById(updateId)).thenReturn(Optional.of(t1));
            when(transactionRepository.save(any())).thenReturn(t2);
            when(transactionWithUsernameRepository.findById(updateId)).thenReturn(Optional.of(out));

            TransactionResponse result = transactionService.updateTransaction(updateId, req);

            assertNotNull(result);
            assertEquals(newDescription, result.getDescription());
            assertEquals(updateId, result.getTransactionId());
            verify(transactionRepository, times(1)).findById(1L);
            verify(transactionMapper, times(1)).updateEntityFromRequest(req, t1);
            verify(transactionRepository, times(1)).save(t2);
            verify(transactionMapper, times(1)).entityWithUsernameToResponse(out);
        }

        @Test
        @DisplayName("Throw exception if transaction is not found.")
        void shouldThrowIfTransactionNotFound() {
            long updateId = 1L;
            TransactionRequest req = createValidTransactionRequest();
            String newDescription = "new description";
            req.setDescription(newDescription);

            when(transactionRepository.findById(updateId)).thenThrow(new ResourceNotFoundException());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> transactionService.updateTransaction(updateId, req));

            assertEquals(RESOURCE_NOT_FOUND.getErrorMessage(), exception.getMessage());
            verify(transactionRepository, times(1)).findById(updateId);
            verifyNoInteractions(transactionMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails.")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            when(transactionRepository.findById(any())).thenThrow(databaseException);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> transactionService.updateTransaction(null, null));

            assertEquals("Database connection timed out", exception.getMessage());
            verify(transactionRepository, times(1)).findById(any());
            verifyNoInteractions(transactionMapper);
        }

    }

    @Nested
    @DisplayName("Tests for deleteTransaction()")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Delete transaction when available.")
        void shouldDeleteTransaction() {
            long deleteId = 1L;

            assertDoesNotThrow(() -> transactionService.deleteTransaction(deleteId));
            verify(transactionRepository, times(1)).deleteById(deleteId);
            verifyNoInteractions(transactionMapper);
        }

        @Test
        @DisplayName("Should propagate runtime exception when repository fails.")
        void shouldPropagateExceptionWhenRepositoryThrows() {
            long deleteId = 1L;
            RuntimeException databaseException = new RuntimeException("Database connection timed out");
            doThrow(databaseException).when(transactionRepository).deleteById(deleteId);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> transactionService.deleteTransaction(deleteId));

            assertEquals("Database connection timed out", exception.getMessage());
            verify(transactionRepository, times(1)).deleteById(deleteId);
            verifyNoInteractions(transactionMapper);
        }
    }
}
