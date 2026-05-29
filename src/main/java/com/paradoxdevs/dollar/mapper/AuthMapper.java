package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.request.AuthRequest;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.model.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface AuthMapper {

    UserDto requestToDto(AuthRequest authRequest);
    User requestToEntity(AuthRequest authRequest);
}
