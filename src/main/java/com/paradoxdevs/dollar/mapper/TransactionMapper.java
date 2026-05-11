package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.model.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface TransactionMapper {

    @Mapping(target="transactionId", source="id")
    @Mapping(target="transactionName", source="name")
    TransactionDto transactionToTransactionDto(Transaction transaction);

    TransactionResponse transactionDtoToTransactionResponse(TransactionDto transactionDto);

    @Mapping(target="id", source="transactionId")
    @Mapping(target="name", source="transactionName")
    Transaction transactionDtoToTransaction(TransactionDto transactionDto);

    @Mapping(target="transactionId", ignore=true)
    TransactionDto transactionRequestToTransactionDto(TransactionRequest transactionRequest);
}
