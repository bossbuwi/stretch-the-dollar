package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.request.AuthRequest;
import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    User requestToEntity(AuthRequest authRequest);

    @Mapping(target = "id", ignore = true)
    UserResponse entityToResponseNoId(User user);
}
