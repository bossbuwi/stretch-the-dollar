package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.aspect.DatabaseExecution;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.model.TransactionDto;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @DatabaseExecution("Get all transactions.")
    @Override
    public List<TransactionDto> getTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @DatabaseExecution("Get specific transaction by ID.")
    @Override
    public TransactionDto getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::entityToDto)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @DatabaseExecution("Add new transaction.")
    @Override
    public TransactionDto addTransaction(TransactionDto transactionDto) {
        Transaction input = transactionMapper.dtoToEntity(transactionDto);
        Transaction output = transactionRepository.save(input);
        return transactionMapper.entityToDto(output);
    }

    @DatabaseExecution("Update an existing transaction.")
    @Override
    public TransactionDto updateTransaction(Long id, TransactionDto update) {
        return transactionRepository.findById(id)
                .map(existing -> {
                    transactionMapper.updateEntityFromDto(update, existing);
                    return transactionRepository.save(existing);
                })
                .map(transactionMapper::entityToDto)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @DatabaseExecution("Delete an existing transaction.")
    @Override
    public void deleteTransaction(long id) {
        transactionRepository.deleteById(id);
    }
}
