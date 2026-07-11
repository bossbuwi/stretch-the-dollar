package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.response.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAvailableRoles();
}
