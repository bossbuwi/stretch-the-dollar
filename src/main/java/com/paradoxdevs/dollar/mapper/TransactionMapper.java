package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel="spring")
public interface TransactionMapper {

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target="transactionName", source="name")
    TransactionResponse entityToResponse(Transaction entity);

    @Mapping(target = "name", source = "transactionName")
    @Mapping(target = "id", ignore = true)
    Transaction requestToEntity(TransactionRequest request);

    @Mapping(target = "name", source = "transactionName")
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(TransactionRequest request, @MappingTarget Transaction entity);
}
