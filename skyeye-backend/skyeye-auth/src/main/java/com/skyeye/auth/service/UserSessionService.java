package com.skyeye.auth.service;

import com.skyeye.auth.dto.UserSessionInfo;
import com.skyeye.auth.entity.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户会话服务接口
 */
public interface UserSessionService {

    /**
     * 创建用户会话
     */
    UserSession createSession(Long userId, String sessionToken, String refreshToken, 
                             String ipAddress, String userAgent, String deviceInfo);

    /**
     * 根据会话令牌获取会话
     */
    UserSession getSessionByToken(String sessionToken);

    /**
     * 根据刷新令牌获取会话
     */
    UserSession getSessionByRefreshToken(String refreshToken);

    /**
     * 更新会话最后访问时间
     */
    void updateLastAccessed(String sessionToken);

    /**
     * 注销会话
     */
    void logoutSession(String sessionToken);

    /**
     * 注销用户的所有会话
     */
    void logoutAllUserSessions(Long userId);

    /**
     * 获取用户的活跃会话列表
     */
    List<UserSessionInfo> getUserActiveSessions(Long userId);

    /**
     * 分页获取用户会话历史
     */
    Page<UserSessionInfo> getUserSessionHistory(Long userId, Pageable pageable);

    /**
     * 获取所有活跃会话
     */
    List<UserSessionInfo> getAllActiveSessions();

    /**
     * 清理过期会话
     */
    int cleanupExpiredSessions();

    /**
     * 验证会话是否有效
     */
    boolean isSessionValid(String sessionToken);

    /**
     * 刷新会话
     */
    UserSession refreshSession(String refreshToken, String newSessionToken, String newRefreshToken);

    /**
     * 统计用户活跃会话数量
     */
    long countUserActiveSessions(Long userId);

    /**
     * 根据IP地址获取活跃会话
     */
    List<UserSessionInfo> getActiveSessionsByIpAddress(String ipAddress);

    /**
     * 强制注销会话
     */
    void forceLogoutSession(Long sessionId);
}