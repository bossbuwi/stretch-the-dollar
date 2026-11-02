package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionDetailsResponse;

import java.util.List;

public interface TransactionService {

    List<TransactionDetailsResponse> getTransactions();
    TransactionDetailsResponse addTransaction(TransactionRequest request);
    TransactionDetailsResponse updateTransaction(Long id, TransactionRequest request);
    TransactionDetailsResponse deleteTransaction(Long id);
    TransactionDetailsResponse getTransactionById(Long id);
}
