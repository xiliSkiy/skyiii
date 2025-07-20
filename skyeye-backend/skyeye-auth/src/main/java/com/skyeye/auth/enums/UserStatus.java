package com.skyeye.auth.enums;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    ACTIVE("激活", "用户账户正常激活状态"),
    INACTIVE("未激活", "用户账户未激活"),
    LOCKED("锁定", "用户账户被锁定"),
    DISABLED("禁用", "用户账户被禁用");

    private final String displayName;
    private final String description;

    UserStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}