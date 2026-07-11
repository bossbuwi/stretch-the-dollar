package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.aspect.annotation.PerformanceMetrics;
import com.paradoxdevs.dollar.aspect.annotation.WithPermission;
import com.paradoxdevs.dollar.constant.PermissionType;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@WithPermission(notAllowedRole = Role.BANNED)
@PerformanceMetrics
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/index")
    public ResponseEntity<List<UserResponse>> index() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username")
    public ResponseEntity<UserResponse> getUserByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/uuid")
    public ResponseEntity<UserResponse> getUserByUuid(@RequestParam String uuid) {
        return ResponseEntity.ok(userService.getUserByUuid(uuid));
    }

    @WithPermission(permission = PermissionType.NONSELF)
    @PutMapping("/admin/make")
    public ResponseEntity<Boolean> makeAdmin(@RequestParam String uuid) {
        userService.makeAdmin(uuid);
        return ResponseEntity.noContent().build();
    }

    @WithPermission(permission = PermissionType.NONSELF)
    @PutMapping("/admin/remove")
    public ResponseEntity<Boolean> demoteAdmin(@RequestParam String uuid) {
        userService.demoteAdmin(uuid);
        return ResponseEntity.noContent().build();
    }

    @WithPermission(permission = PermissionType.NONSELF)
    @PutMapping("/restricted/make")
    public ResponseEntity<Boolean> restrictUser(@RequestParam String uuid) {
        userService.restrictUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @WithPermission(permission = PermissionType.NONSELF)
    @PutMapping("/restricted/remove")
    public ResponseEntity<Boolean> allowUser(@RequestParam String uuid) {
        userService.allowUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @WithPermission(permission = PermissionType.NONSELF)
    @PutMapping("/ban/make")
    public ResponseEntity<Boolean> banUser(@RequestParam String uuid) {
        userService.banUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @WithPermission(permission = PermissionType.NONSELF)
    @PutMapping("/ban/remove")
    public ResponseEntity<Boolean> unbanUser(@RequestParam String uuid) {
        userService.unbanUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @WithPermission(permission = PermissionType.NONE)
    @PutMapping("/reset")
    public ResponseEntity<Boolean> resetUser(@RequestParam String uuid) {
        userService.resetUser(uuid);
        return ResponseEntity.noContent().build();
    }
}
