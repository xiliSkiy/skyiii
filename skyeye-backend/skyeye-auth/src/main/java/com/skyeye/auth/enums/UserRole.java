package com.skyeye.auth.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    SYSTEM_ADMIN("系统管理员", "拥有系统所有权限"),
    SECURITY_ADMIN("安全管理员", "负责安全相关功能管理"),
    OPERATOR("操作员", "负责日常监控操作"),
    VIEWER("查看者", "只能查看监控画面和报告");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
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