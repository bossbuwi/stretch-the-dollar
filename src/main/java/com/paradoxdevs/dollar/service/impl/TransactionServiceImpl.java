package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.aspect.annotation.CheckOwnership;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.entity.TransactionWithUsername;
import com.paradoxdevs.dollar.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.repository.TransactionWithUsernameRepository;
import com.paradoxdevs.dollar.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionWithUsernameRepository transactionWithUsernameRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  TransactionWithUsernameRepository transactionWithUsernameRepository,
                                  TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionWithUsernameRepository = transactionWithUsernameRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public List<TransactionResponse> getTransactions() {
        return transactionWithUsernameRepository.findAll()
                .stream()
                .map(transactionMapper::entityWithUsernameToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {
        return transactionWithUsernameRepository.findById(id)
                .map(transactionMapper::entityWithUsernameToResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    @Override
    public TransactionResponse addTransaction(TransactionRequest request) {
        Transaction input = transactionMapper.requestToEntity(request);
        Transaction saved = transactionRepository.save(input);
        TransactionWithUsername output = transactionWithUsernameRepository.findById(saved.getId())
                .orElseThrow(ResourceNotFoundException::new);
        return transactionMapper.entityWithUsernameToResponse(output);
    }

    @CheckOwnership(Transaction.class)
    @Transactional
    @Override
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction existing = transactionRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        transactionMapper.updateEntityFromRequest(request, existing);
        Transaction saved = transactionRepository.saveAndFlush(existing);
        TransactionWithUsername output = transactionWithUsernameRepository.findById(saved.getId())
                .orElseThrow(ResourceNotFoundException::new);
        return transactionMapper.entityWithUsernameToResponse(output);
    }

    @CheckOwnership(Transaction.class)
    @Override
    public void deleteTransaction(long id) {
        // TODO: Find a way to enable this method to
        //  throw an exception if the provided id
        //  does not exist.
        transactionRepository.deleteById(id);
    }
}
