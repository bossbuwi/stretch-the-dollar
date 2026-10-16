package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.TransactionCategoryResponse;

import java.util.List;

public interface TransactionCategoryService {
    List<TransactionCategoryResponse> getTransactionItems();
    TransactionCategoryResponse getTransactionItemById(Long id);
    TransactionCategoryResponse addTransactionItem(TransactionCategoryRequest transactionCategoryRequest);
}
