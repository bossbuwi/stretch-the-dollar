package com.paradoxdevs.dollar.entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN, USER, RESTRICTED, BANNED;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
