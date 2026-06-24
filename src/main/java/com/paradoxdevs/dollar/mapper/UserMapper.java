package com.paradoxdevs.dollar.mapper;

import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel="spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleToString")
    UserResponse entityToResponse(User user);

    @Named("roleToString")
    default String roleToString(Role role) {
        return role.toString();
    }
}
