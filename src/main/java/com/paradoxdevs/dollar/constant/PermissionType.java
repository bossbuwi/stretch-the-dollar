package com.paradoxdevs.dollar.constant;

import lombok.Getter;

@Getter
public enum PermissionType {
    ALL("ALL"),
    SELF("SELF"),
    ADMIN("ADMIN"),
    NONE("NONE");

    private final String permission;

    PermissionType(String permission) {
        this.permission = permission;
    }
}
