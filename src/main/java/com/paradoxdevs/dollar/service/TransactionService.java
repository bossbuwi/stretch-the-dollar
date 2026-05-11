package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.model.TransactionDto;

import java.util.List;

public interface TransactionService {
    List<TransactionDto> getTransactions();
    TransactionDto getTransactionById(Long id);
    TransactionDto addTransaction(TransactionDto transaction);
    TransactionDto updateTransaction(Long id, TransactionDto transaction);
    void deleteTransaction(long id);
}
