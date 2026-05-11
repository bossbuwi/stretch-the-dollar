package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.mapper.TransactionMapper;
import com.paradoxdevs.dollar.model.TransactionDto;
import com.paradoxdevs.dollar.service.TransactionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @GetMapping("/index")
    public List<TransactionResponse> getTransactions() {
        List<TransactionResponse> response = new ArrayList<>();

        List<TransactionDto> transactions = transactionService.getTransactions();
        for (TransactionDto transactionDto : transactions) {
            TransactionResponse transactionResponse = transactionMapper.dtoToResponse(transactionDto);
            response.add(transactionResponse);
        }

        return response;
    }

    @GetMapping("/")
    public TransactionResponse getTransaction(@RequestParam("id") long id) {
        TransactionDto transactionDto = transactionService.getTransactionById(id);
        return transactionMapper.dtoToResponse(transactionDto);
    }

    @PostMapping("/")
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {
        TransactionDto dto = transactionService.addTransaction(transactionMapper.requestToDto(request));
        return transactionMapper.dtoToResponse(dto);
    }

    @PutMapping("/{id}")
    public TransactionResponse updateTransaction(@PathVariable Long id, @RequestBody TransactionRequest request) {
        TransactionDto dto = transactionService.updateTransaction(id, transactionMapper.requestToDto(request));
        return transactionMapper.dtoToResponse(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}
