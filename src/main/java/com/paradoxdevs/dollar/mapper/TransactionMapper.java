package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.model.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel="spring")
public interface TransactionMapper {

    @Mapping(target="transactionId", source="id")
    @Mapping(target="transactionName", source="name")
    TransactionDto entityToDto(Transaction transaction);

    TransactionResponse dtoToResponse(TransactionDto transactionDto);

    @Mapping(target="id", source="transactionId")
    @Mapping(target="name", source="transactionName")
    Transaction dtoToEntity(TransactionDto transactionDto);

    @Mapping(target="transactionId", ignore=true)
    TransactionDto requestToDto(TransactionRequest transactionRequest);

    @Mapping(target = "name", source = "transactionName")
    void updateEntityFromDto(TransactionDto transactionDto, @MappingTarget Transaction transaction);
}
