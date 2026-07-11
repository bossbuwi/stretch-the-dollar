package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.response.RoleResponse;
import com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics;
import com.paradoxdevs.dollar.aspect.annotation.WithPermission;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@WithPermission(notAllowedRole = Role.BANNED)
@PerformanceMetrics
@Slf4j
@RestController
@RequestMapping("/role")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/index")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAvailableRoles());
    }
}
