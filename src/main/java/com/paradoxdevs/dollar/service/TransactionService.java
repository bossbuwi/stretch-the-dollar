package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;

import java.util.List;

public interface TransactionService {
    List<TransactionResponse> getTransactions();
    TransactionResponse getTransactionById(Long id);
    TransactionResponse addTransaction(TransactionRequest request);
    TransactionResponse updateTransaction(Long id, TransactionRequest request);
    void deleteTransaction(Long id);
}
