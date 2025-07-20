package com.skyeye.common.util;

/**
 * 安全工具类
 */
public class SecurityUtils {

    // 使用ThreadLocal存储当前用户信息，避免直接依赖Spring Security
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    /**
     * 设置当前用户名
     */
    public static void setCurrentUsername(String username) {
        currentUsername.set(username);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        String username = currentUsername.get();
        return username != null ? username : "anonymous";
    }

    /**
     * 清除当前用户信息
     */
    public static void clearCurrentUser() {
        currentUserId.remove();
        currentUsername.remove();
    }

    /**
     * 检查是否已认证
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
}