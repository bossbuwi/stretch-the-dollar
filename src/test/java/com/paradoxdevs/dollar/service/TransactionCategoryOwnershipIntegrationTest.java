package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.TransactionCategoryResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.repository.TransactionCategoryRepository;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class TransactionCategoryOwnershipIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;
    @Autowired
    private TransactionCategoryService transactionCategoryService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Category creator can update and remains the owner")
    void creatorCanUpdateCategory() {
        User creator = saveUser("category-owner");
        TransactionCategory category = saveCategoryAs(creator, "Original category");

        setAuthentication(creator.getUuid(), Role.USER);
        TransactionCategoryResponse response = transactionCategoryService.updateTransactionItem(
                category.getId(), request("Edited category", "INCOME"));
        TransactionCategory updated = transactionCategoryRepository.findById(category.getId()).orElseThrow();

        assertEquals("Edited category", response.getName());
        assertEquals("INCOME", response.getTransactionType());
        assertEquals(creator.getUuid(), updated.getCreatedBy());
        assertEquals(creator.getUuid().toString(), response.getCreatedBy());
        assertNotNull(updated.getCreatedAt());
        assertEquals(updated.getCreatedAt().truncatedTo(ChronoUnit.SECONDS), Instant.parse(response.getCreatedAt()));
        assertEquals(creator.getUuid(), updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
        assertEquals(updated.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS), Instant.parse(response.getUpdatedAt()));
    }

    @Test
    @DisplayName("Admin update records the authenticated editor and preserves the creator")
    void adminUpdateRecordsEditorAndKeepsCreator() {
        User creator = saveUser("category-original-owner");
        User editor = saveUser("category-admin-editor");
        TransactionCategory category = saveCategoryAs(creator, "Admin editable category");
        Instant createdAt = category.getCreatedAt();

        setAuthentication(editor.getUuid(), Role.ADMIN);
        TransactionCategoryResponse response = transactionCategoryService.updateTransactionItem(
                category.getId(), request("Edited by admin", "INCOME"));
        TransactionCategory updated = transactionCategoryRepository.findById(category.getId()).orElseThrow();

        assertEquals(creator.getUuid(), updated.getCreatedBy());
        assertEquals(creator.getUuid().toString(), response.getCreatedBy());
        assertEquals(createdAt, updated.getCreatedAt());
        assertEquals(editor.getUuid(), updated.getUpdatedBy());
        assertEquals(editor.getUuid().toString(), response.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
        assertFalse(updated.getUpdatedAt().isBefore(createdAt));
        assertEquals(updated.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS), Instant.parse(response.getUpdatedAt()));
    }

    @Test
    @DisplayName("Non-owner cannot update category")
    void nonOwnerCannotUpdateCategory() {
        User creator = saveUser("category-real-owner");
        User otherUser = saveUser("category-other-user");
        TransactionCategory category = saveCategoryAs(creator, "Protected category");
        setAuthentication(otherUser.getUuid(), Role.USER);

        assertThrows(AccessDeniedException.class, () -> transactionCategoryService.updateTransactionItem(
                category.getId(), request("Unauthorized edit", "EXPENSE")));

        assertEquals("Protected category",
                transactionCategoryRepository.findById(category.getId()).orElseThrow().getName());
    }

    @Test
    @DisplayName("Admin can update category with no recorded owner")
    void adminCanUpdateOwnerlessLegacyCategory() {
        User admin = saveUser("category-legacy-admin");
        TransactionCategory category = new TransactionCategory();
        category.setName("Legacy category");
        category.setTransactionType("EXPENSE");
        category = transactionCategoryRepository.saveAndFlush(category);
        Long categoryId = category.getId();
        assertNull(category.getCreatedBy());
        setAuthentication(admin.getUuid(), Role.ADMIN);

        TransactionCategoryResponse response = transactionCategoryService.updateTransactionItem(
                categoryId, request("Updated legacy category", "INCOME"));

        assertEquals("Updated legacy category", response.getName());
        assertEquals("INCOME", response.getTransactionType());
    }

    @Test
    @DisplayName("Non-admin cannot update ownerless legacy category")
    void nonAdminCannotUpdateOwnerlessLegacyCategory() {
        User user = saveUser("category-legacy-user");
        TransactionCategory category = new TransactionCategory();
        category.setName("Ownerless category");
        category.setTransactionType("EXPENSE");
        category = transactionCategoryRepository.saveAndFlush(category);
        Long categoryId = category.getId();
        setAuthentication(user.getUuid(), Role.USER);

        assertThrows(AccessDeniedException.class, () -> transactionCategoryService.updateTransactionItem(
                categoryId, request("Forbidden edit", "INCOME")));
    }

    private User saveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("unused-test-password");
        user.setUuid(UUID.randomUUID());
        return userRepository.saveAndFlush(user);
    }

    private TransactionCategory saveCategoryAs(User creator, String name) {
        setAuthentication(creator.getUuid(), Role.USER);
        TransactionCategory category = new TransactionCategory();
        category.setName(name);
        category.setTransactionType("EXPENSE");
        return transactionCategoryRepository.saveAndFlush(category);
    }

    private TransactionCategoryRequest request(String name, String type) {
        return TransactionCategoryRequest.builder()
                .name(name)
                .description("Edited category")
                .transactionType(type)
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
