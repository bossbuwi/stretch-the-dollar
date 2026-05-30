package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.request.AuthRequest;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.model.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    UserDto requestToDto(AuthRequest authRequest);
    @Mapping(target = "id", ignore = true)
    User requestToEntity(AuthRequest authRequest);
}
