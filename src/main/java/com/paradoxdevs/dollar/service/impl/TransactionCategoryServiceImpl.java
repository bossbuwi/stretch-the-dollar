package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.TransactionCategoryResponse;
import com.paradoxdevs.dollar.aspect.annotation.CheckOwnership;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import com.paradoxdevs.dollar.entity.view.TransactionCategoryDetails;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.TransactionCategoryMapper;
import com.paradoxdevs.dollar.repository.TransactionCategoryRepository;
import com.paradoxdevs.dollar.repository.view.TransactionCategoryDetailsView;
import com.paradoxdevs.dollar.service.TransactionCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TransactionCategoryServiceImpl implements TransactionCategoryService {
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionCategoryDetailsView transactionCategoryDetailsView;
    private final TransactionCategoryMapper transactionCategoryMapper;

    public TransactionCategoryServiceImpl(TransactionCategoryRepository transactionCategoryRepository,
                                          TransactionCategoryDetailsView transactionCategoryDetailsView,
                                          TransactionCategoryMapper transactionCategoryMapper) {
        this.transactionCategoryRepository = transactionCategoryRepository;
        this.transactionCategoryDetailsView = transactionCategoryDetailsView;
        this.transactionCategoryMapper = transactionCategoryMapper;
    }

    @Override
    public List<TransactionCategoryResponse> getTransactionItems() {
        return transactionCategoryDetailsView.findAll()
                .stream()
                .map(transactionCategoryMapper::viewToResponse)
                .toList();
    }

    @Override
    public TransactionCategoryResponse getTransactionItemById(Long id) {
        return transactionCategoryDetailsView.findById(id)
                .map(transactionCategoryMapper::viewToResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    @Override
    public TransactionCategoryResponse addTransactionItem(TransactionCategoryRequest request) {
        TransactionCategory input = transactionCategoryMapper.requestToEntity(request);
        TransactionCategory saved = transactionCategoryRepository.saveAndFlush(input);
        TransactionCategoryDetails details = transactionCategoryDetailsView.findById(saved.getId())
                .orElseThrow(ResourceNotFoundException::new);
        return transactionCategoryMapper.viewToResponse(details);
    }

    @Transactional
    @CheckOwnership(TransactionCategory.class)
    @Override
    public TransactionCategoryResponse updateTransactionItem(Long id, TransactionCategoryRequest request) {
        TransactionCategory category = transactionCategoryRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        transactionCategoryMapper.updateEntityFromRequest(request, category);
        TransactionCategory saved = transactionCategoryRepository.saveAndFlush(category);
        TransactionCategoryDetails details = transactionCategoryDetailsView.findById(saved.getId())
                .orElseThrow(ResourceNotFoundException::new);
        return transactionCategoryMapper.viewToResponse(details);
    }
}
