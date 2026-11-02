package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionDetailsResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.repository.TransactionCategoryRepository;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TransactionAuditIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Update transaction records authenticated editor and modification time")
    void updateTransaction_recordsAuthenticatedEditorAndModificationTime() {
        User creator = saveUser("transaction-creator");
        User editor = saveUser("transaction-editor");
        TransactionCategory category = new TransactionCategory();
        category.setName("transaction-test-category");
        category.setTransactionType("EXPENSE");
        category = transactionCategoryRepository.saveAndFlush(category);

        setAuthentication(creator.getUuid(), Role.USER);
        Transaction transaction = new Transaction();
        transaction.setName("Original transaction");
        transaction.setTransactionCategoryId(category.getId());
        transaction.setAmount(new BigDecimal("10.00"));
        transaction.setCurrency("USD");
        transaction.setTransactionDate(LocalDate.parse("2026-01-01"));
        transaction = transactionRepository.saveAndFlush(transaction);
        Instant originalUpdatedAt = transaction.getUpdatedAt();

        setAuthentication(editor.getUuid(), Role.ADMIN);
        TransactionRequest request = TransactionRequest.builder()
                .name("Edited transaction")
                .description("Edited by admin")
                .transactionCategoryId(category.getId())
                .transactionDate("2026-02-01")
                .amount(new BigDecimal("20.00"))
                .currency("USD")
                .build();

        TransactionDetailsResponse response = transactionService.updateTransaction(transaction.getId(), request);
        Transaction updated = transactionRepository.findById(transaction.getId()).orElseThrow();

        assertEquals(editor.getUuid(), updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(originalUpdatedAt));
        assertEquals("transaction-editor", response.getUpdatedBy());
        assertEquals(updated.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS), Instant.parse(response.getUpdatedAt()));
    }

    @Test
    @DisplayName("Delete transaction returns deleted details and removes transaction")
    void deleteTransaction_returnsDeletedDetailsAndRemovesTransaction() {
        User creator = saveUser("transaction-delete-creator");
        TransactionCategory category = saveCategory("transaction-delete-category");
        setAuthentication(creator.getUuid(), Role.USER);

        Transaction transaction = new Transaction();
        transaction.setName("Deleted transaction");
        transaction.setDescription("Transaction details before deletion");
        transaction.setTransactionCategoryId(category.getId());
        transaction.setAmount(new BigDecimal("42.75"));
        transaction.setCurrency("USD");
        transaction.setTransactionDate(LocalDate.parse("2026-03-10"));
        transaction = transactionRepository.saveAndFlush(transaction);
        Long id = transaction.getId();

        TransactionDetailsResponse response = transactionService.deleteTransaction(id);

        assertEquals(id, response.getId());
        assertEquals("Deleted transaction", response.getName());
        assertEquals("Transaction details before deletion", response.getDescription());
        assertEquals("transaction-delete-creator", response.getCreatedBy());
        assertFalse(transactionRepository.findById(id).isPresent());
    }

    @Test
    @DisplayName("Delete transaction rejects non owner and preserves transaction")
    void deleteTransaction_rejectsNonOwnerAndPreservesTransaction() {
        User creator = saveUser("transaction-delete-owner");
        User otherUser = saveUser("transaction-delete-other");
        TransactionCategory category = saveCategory("transaction-delete-denied-category");
        setAuthentication(creator.getUuid(), Role.USER);

        Transaction transaction = new Transaction();
        transaction.setName("Protected transaction");
        transaction.setTransactionCategoryId(category.getId());
        transaction.setAmount(new BigDecimal("42.75"));
        transaction.setCurrency("USD");
        transaction.setTransactionDate(LocalDate.parse("2026-03-10"));
        transaction = transactionRepository.saveAndFlush(transaction);
        Long id = transaction.getId();

        setAuthentication(otherUser.getUuid(), Role.USER);

        assertThrows(AccessDeniedException.class, () -> transactionService.deleteTransaction(id));
        assertTrue(transactionRepository.findById(id).isPresent());
    }

    private User saveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("unused-test-password");
        user.setUuid(UUID.randomUUID());
        return userRepository.saveAndFlush(user);
    }

    private TransactionCategory saveCategory(String name) {
        TransactionCategory category = new TransactionCategory();
        category.setName(name);
        category.setTransactionType("EXPENSE");
        return transactionCategoryRepository.saveAndFlush(category);
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
