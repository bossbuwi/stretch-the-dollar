package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.model.TransactionDto;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public List<TransactionDto> getTransactions() {
        List<TransactionDto> transactions = new ArrayList<>();
        transactionRepository.findAll().forEach(transaction -> {
            TransactionDto dto = transactionMapper.transactionToTransactionDto(transaction);
            transactions.add(dto);
        });
        return transactions;
    }

    @Override
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id).orElse(null);
        return transactionMapper.transactionToTransactionDto(transaction);
    }

    @Override
    public TransactionDto addTransaction(TransactionDto transactionDto) {
        Transaction input = transactionMapper.transactionDtoToTransaction(transactionDto);
        Transaction output = transactionRepository.save(input);
        return transactionMapper.transactionToTransactionDto(output);
    }

    @Override
    public TransactionDto updateTransaction(Long id, TransactionDto transaction) {
        Transaction current = transactionRepository.findById(id).orElse(null);
        Transaction update = transactionMapper.transactionDtoToTransaction(transaction);
        BeanUtils.copyProperties(update, current, "id");
        Transaction output = transactionRepository.save(current);
        return transactionMapper.transactionToTransactionDto(output);
    }

    @Override
    public void deleteTransaction(long id) {
        transactionRepository.deleteById(id);
    }
}
