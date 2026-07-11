package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.response.RoleResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Override
    public List<RoleResponse> getAvailableRoles() {
        List<RoleResponse> roles = new ArrayList<>();
        for (Role role: Role.values()) {
            RoleResponse roleResponse = new RoleResponse();
            roleResponse.setRoleName(role.name());
            roles.add(roleResponse);
        }
        return roles;
    }
}
