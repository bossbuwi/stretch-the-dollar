package com.paradoxdevs.dollar.helper;

import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.User;

import java.util.UUID;

public final class UserDataHelper {
    public static final long USER_ID = 1L;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final UUID USER_UUID = UUID.randomUUID();

    public static User createValidUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setUuid(USER_UUID);
        user.addRole(Role.USER);
        return user;
    }
}
