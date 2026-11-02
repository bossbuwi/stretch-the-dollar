package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.request.TransactionCategoryRequest;
import com.paradoxdevs.dollar.api.response.TransactionCategoryResponse;
import com.paradoxdevs.dollar.entity.TransactionCategory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Mapper(componentModel="spring")
public interface TransactionCategoryMapper {

    @Mapping(target = "id", ignore = true)
    TransactionCategory requestToEntity(TransactionCategoryRequest from);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "transactionType", source = "transactionType")
    void updateEntityFromRequest(TransactionCategoryRequest request, @MappingTarget TransactionCategory category);

    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "uuidToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToString")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "uuidToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "instantToString")
    TransactionCategoryResponse entityToResponse(TransactionCategory from);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

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
