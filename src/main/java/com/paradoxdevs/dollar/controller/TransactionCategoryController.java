package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.TransactionCategoryResponse;
import com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics;
import com.paradoxdevs.dollar.aspect.annotation.WithPermission;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.service.TransactionCategoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@WithPermission(notAllowedRole = Role.BANNED)
@PerformanceMetrics
@Slf4j
@RestController
@RequestMapping("/transaction-item")
public class TransactionCategoryController {
    private final TransactionCategoryService transactionCategoryService;

    public TransactionCategoryController(TransactionCategoryService transactionCategoryService) {
        this.transactionCategoryService = transactionCategoryService;
    }

    @GetMapping("/index")
    public ResponseEntity<List<TransactionCategoryResponse>> getTransactionItems() {
        return ResponseEntity.ok(transactionCategoryService.getTransactionItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionCategoryResponse> getTransactionItemById(@PathVariable String id) {
        return null;
    }

    @PostMapping("/")
    public ResponseEntity<TransactionCategoryResponse> addTransactionItem(@RequestBody @Valid TransactionCategoryRequest request) {
        return ResponseEntity.ok(transactionCategoryService.addTransactionItem(request));
    }
}
