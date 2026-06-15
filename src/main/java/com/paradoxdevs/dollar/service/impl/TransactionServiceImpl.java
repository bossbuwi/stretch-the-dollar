package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.service.TransactionService;
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

    @PerformanceMetrics
    @Override
    public List<TransactionResponse> getTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::entityToResponse)
                .collect(Collectors.toList());
    }

    @PerformanceMetrics
    @Override
    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::entityToResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @PerformanceMetrics
    @Override
    public TransactionResponse addTransaction(TransactionRequest request) {
        Transaction input = transactionMapper.requestToEntity(request);
        Transaction output = transactionRepository.save(input);
        return transactionMapper.entityToResponse(output);
    }

    @PerformanceMetrics
    @Override
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        return transactionRepository.findById(id)
                .map(existing -> {
                    transactionMapper.updateEntityFromRequest(request, existing);
                    return transactionRepository.save(existing);
                })
                .map(transactionMapper::entityToResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @PerformanceMetrics
    @Override
    public void deleteTransaction(long id) {
        // TODO: Find a way to enable this method to
        //  throw an exception if the provided id
        //  does not exist.
        transactionRepository.deleteById(id);
    }
}
