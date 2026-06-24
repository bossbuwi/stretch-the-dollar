package com.paradoxdevs.dollar.controller;

import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/available-roles")
    public ResponseEntity<UserResponse> getRoles() {
        return ResponseEntity.ok(userService.getAvailableRoles());
    }

    @PutMapping("/admin/make")
    public ResponseEntity<Boolean> makeAdmin(@RequestParam String uuid) {
        userService.makeAdmin(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/remove")
    public ResponseEntity<Boolean> demoteAdmin(@RequestParam String uuid) {
        userService.demoteAdmin(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/restricted/make")
    public ResponseEntity<Boolean> restrictUser(@RequestParam String uuid) {
        userService.restrictUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/restricted/remove")
    public ResponseEntity<Boolean> allowUser(@RequestParam String uuid) {
        userService.allowUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ban/make")
    public ResponseEntity<Boolean> banUser(@RequestParam String uuid) {
        userService.banUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/ban/remove")
    public ResponseEntity<Boolean> unbanUser(@RequestParam String uuid) {
        userService.unbanUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reset")
    public ResponseEntity<Boolean> resetUser(@RequestParam String uuid) {
        userService.resetUser(uuid);
        return ResponseEntity.noContent().build();
    }
}
