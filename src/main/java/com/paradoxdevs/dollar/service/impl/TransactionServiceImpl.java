package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionDetailsResponse;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.entity.view.TransactionDetails;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.repository.TransactionRepository;
import com.paradoxdevs.dollar.repository.view.TransactionDetailsView;
import com.paradoxdevs.dollar.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionDetailsView transactionDetailsView;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  TransactionDetailsView transactionDetailsView,
                                  TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionDetailsView = transactionDetailsView;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public List<TransactionDetailsResponse> getTransactions() {
        return transactionDetailsView.findAll()
                .stream()
                .map(transactionMapper::viewToResponse)
                .toList();
    }

    @Override
    public TransactionDetailsResponse getTransactionById(Long id) {
        return transactionDetailsView.findById(id)
                .map(transactionMapper::viewToResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    @Override
    public TransactionDetailsResponse addTransaction(TransactionRequest request) {
        Transaction input = transactionMapper.requestToEntity(request);
        Transaction saved = transactionRepository.save(input);
        TransactionDetails output = transactionDetailsView.findById(saved.getId())
                .orElseThrow(ResourceNotFoundException::new);
        return transactionMapper.viewToResponse(output);
    }
}
