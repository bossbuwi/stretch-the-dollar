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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private User saveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("unused-test-password");
        user.setUuid(UUID.randomUUID());
        return userRepository.saveAndFlush(user);
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
