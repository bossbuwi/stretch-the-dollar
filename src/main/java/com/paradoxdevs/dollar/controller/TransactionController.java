package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionDetailsResponse;
import com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics;
import com.paradoxdevs.dollar.aspect.annotation.WithPermission;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.service.TransactionService;
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
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/index")
    public ResponseEntity<List<TransactionDetailsResponse>> getTransactions() {
        return ResponseEntity.ok(transactionService.getTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailsResponse> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @WithPermission(notAllowedRole = Role.RESTRICTED)
    @PostMapping("/")
    public ResponseEntity<TransactionDetailsResponse> addTransaction(@RequestBody @Valid TransactionRequest request) {
        return ResponseEntity.ok(transactionService.addTransaction(request));
    }
}
