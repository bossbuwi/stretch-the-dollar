package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getUsers();
    UserResponse getUserById(Long id);
    UserResponse getUserByUsername(String username);
    UserResponse getUserByUuid(String uuid);
    void makeAdmin(String uuid);
    void demoteAdmin(String uuid);
    void restrictUser(String uuid);
    void allowUser(String uuid);
    void banUser(String uuid);
    void unbanUser(String uuid);
    void resetUser(String uuid);
}
