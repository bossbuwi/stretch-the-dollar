package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.request.TransactionRequest;
import com.paradoxdevs.dollar.api.response.TransactionResponse;
import com.paradoxdevs.dollar.entity.Transaction;
import com.paradoxdevs.dollar.entity.TransactionWithUsername;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel="spring")
public interface TransactionMapper {

    @Mapping(target = "name", source = "transactionName")
    @Mapping(target = "id", ignore = true)
    Transaction requestToEntity(TransactionRequest request);

    @Mapping(target = "name", source = "transactionName")
    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(TransactionRequest request, @MappingTarget Transaction entity);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "transactionName", source = "name")
    @Mapping(target = "createdBy", source = "createdByUsername")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToString")
    @Mapping(target = "updatedBy", source = "updatedByUsername")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToString")
    TransactionResponse entityWithUsernameToResponse(TransactionWithUsername entity);

    @Named("instantToString")
    default String instantToString(Instant instant) {
        if (instant == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);
        return formatter.format(instant);
    }
}
