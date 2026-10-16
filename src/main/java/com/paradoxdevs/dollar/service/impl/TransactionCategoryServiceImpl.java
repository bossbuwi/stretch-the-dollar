package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.TransactionCategoryResponse;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import com.paradoxdevs.dollar.mapper.TransactionCategoryMapper;
import com.paradoxdevs.dollar.repository.TransactionCategoryRepository;
import com.paradoxdevs.dollar.service.TransactionCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TransactionCategoryServiceImpl implements TransactionCategoryService {
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionCategoryMapper transactionCategoryMapper;

    public TransactionCategoryServiceImpl(TransactionCategoryRepository transactionCategoryRepository,
                                          TransactionCategoryMapper transactionCategoryMapper) {
        this.transactionCategoryRepository = transactionCategoryRepository;
        this.transactionCategoryMapper = transactionCategoryMapper;
    }

    @Override
    public List<TransactionCategoryResponse> getTransactionItems() {
        return transactionCategoryRepository.findAll()
                .stream()
                .map(transactionCategoryMapper::entityToResponse)
                .toList();
    }

    @Transactional
    @Override
    public TransactionCategoryResponse addTransactionItem(TransactionCategoryRequest request) {
        TransactionCategory input = transactionCategoryMapper.requestToEntity(request);
        TransactionCategory saved = transactionCategoryRepository.save(input);
        return transactionCategoryMapper.entityToResponse(saved);
    }
}
