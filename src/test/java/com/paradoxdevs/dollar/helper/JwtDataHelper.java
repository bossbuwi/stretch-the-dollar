package com.paradoxdevs.dollar.helper;

import java.util.ArrayList;
import java.util.List;

public final class JwtDataHelper {
    public final static String USERNAME = "testuser";
    public final static String VALID_TOKEN = "valid.jwt.string";
    public final static String INVALID_TOKEN = "invalid.jwt.string";
    public final static String CORRUPTED_TOKEN = "invalid.jwt.string";

    public static List<String> createRoles() {
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        return roles;
    }
}
