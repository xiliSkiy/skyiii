package com.skyeye.auth.service.impl;

import com.skyeye.auth.dto.UserSessionInfo;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.entity.UserSession;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.repository.UserSessionRepository;
import com.skyeye.auth.service.UserSessionService;
import com.skyeye.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户会话服务实现类
 */
@Service
@Transactional
public class UserSessionServiceImpl implements UserSessionService {

    private static final Logger logger = LoggerFactory.getLogger(UserSessionServiceImpl.class);
    private static final int SESSION_DURATION_HOURS = 24; // 会话持续时间（小时）

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserSession createSession(Long userId, String sessionToken, String refreshToken, 
                                   String ipAddress, String userAgent, String deviceInfo) {
        logger.info("创建用户会话 - 用户ID: {}, IP: {}", userId, ipAddress);
        
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!user.isActive()) {
            throw new BusinessException("用户账户未激活或被锁定");
        }
        
        // 创建会话
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);
        UserSession session = new UserSession(userId, sessionToken, refreshToken, expiresAt);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceInfo(deviceInfo);
        
        UserSession savedSession = userSessionRepository.save(session);
        logger.info("用户会话创建成功 - 会话ID: {}, 用户ID: {}", savedSession.getId(), userId);
        
        return savedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSession getSessionByToken(String sessionToken) {
        logger.debug("根据会话令牌获取会话 - Token: {}", sessionToken.substring(0, Math.min(10, sessionToken.length())) + "...");
        
        UserSession session = userSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new BusinessException("会话不存在"));
        
        if (!session.isValid()) {
            throw new BusinessException("会话已过期或无效");
        }
        
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSession getSessionByRefreshToken(String refreshToken) {
        logger.debug("根据刷新令牌获取会话 - RefreshToken: {}", refreshToken.substring(0, Math.min(10, refreshToken.length())) + "...");
        
        return userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("刷新令牌不存在"));
    }

    @Override
    public void updateLastAccessed(String sessionToken) {
        logger.debug("更新会话最后访问时间 - Token: {}", sessionToken.substring(0, Math.min(10, sessionToken.length())) + "...");
        
        UserSession session = userSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new BusinessException("会话不存在"));
        
        session.updateLastAccessed();
        userSessionRepository.save(session);
    }

    @Override
    public void logoutSession(String sessionToken) {
        logger.info("注销会话 - Token: {}", sessionToken.substring(0, Math.min(10, sessionToken.length())) + "...");
        
        UserSession session = userSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new BusinessException("会话不存在"));
        
        session.logout();
        userSessionRepository.save(session);
        
        logger.info("会话注销成功 - 会话ID: {}", session.getId());
    }

    @Override
    public void logoutAllUserSessions(Long userId) {
        logger.info("注销用户所有会话 - 用户ID: {}", userId);
        
        int loggedOutCount = userSessionRepository.logoutAllUserSessions(userId, LocalDateTime.now());
        
        logger.info("用户所有会话注销完成 - 用户ID: {}, 注销数量: {}", userId, loggedOutCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionInfo> getUserActiveSessions(Long userId) {
        logger.info("获取用户活跃会话 - 用户ID: {}", userId);
        
        List<UserSession> sessions = userSessionRepository.findActiveSessionsByUserId(userId, LocalDateTime.now());
        
        return sessions.stream()
                .map(this::convertToUserSessionInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSessionInfo> getUserSessionHistory(Long userId, Pageable pageable) {
        logger.info("获取用户会话历史 - 用户ID: {}, 分页: {}", userId, pageable);
        
        Page<UserSession> sessionPage = userSessionRepository.findByUserId(userId, pageable);
        
        return sessionPage.map(this::convertToUserSessionInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionInfo> getAllActiveSessions() {
        logger.info("获取所有活跃会话");
        
        List<UserSession> sessions = userSessionRepository.findActiveSessions(LocalDateTime.now());
        
        return sessions.stream()
                .map(this::convertToUserSessionInfo)
                .collect(Collectors.toList());
    }

    @Override
    public int cleanupExpiredSessions() {
        logger.info("清理过期会话");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 先标记过期会话为非活跃状态
        int loggedOutCount = userSessionRepository.logoutExpiredSessions(now, now);
        
        // 删除超过30天的过期会话
        LocalDateTime expiredBefore = now.minusDays(30);
        int deletedCount = userSessionRepository.deleteExpiredSessions(expiredBefore);
        
        logger.info("过期会话清理完成 - 注销数量: {}, 删除数量: {}", loggedOutCount, deletedCount);
        
        return loggedOutCount + deletedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(String sessionToken) {
        logger.debug("验证会话有效性 - Token: {}", sessionToken.substring(0, Math.min(10, sessionToken.length())) + "...");
        
        try {
            UserSession session = userSessionRepository.findBySessionToken(sessionToken)
                    .orElse(null);
            
            return session != null && session.isValid();
        } catch (Exception e) {
            logger.warn("会话验证失败", e);
            return false;
        }
    }

    @Override
    public UserSession refreshSession(String refreshToken, String newSessionToken, String newRefreshToken) {
        logger.info("刷新会话 - RefreshToken: {}", refreshToken.substring(0, Math.min(10, refreshToken.length())) + "...");
        
        UserSession session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("刷新令牌不存在"));
        
        if (!session.getIsActive()) {
            throw new BusinessException("会话已注销");
        }
        
        // 更新会话令牌和刷新令牌
        session.setSessionToken(newSessionToken);
        session.setRefreshToken(newRefreshToken);
        session.setExpiresAt(LocalDateTime.now().plusHours(SESSION_DURATION_HOURS));
        session.updateLastAccessed();
        
        UserSession savedSession = userSessionRepository.save(session);
        logger.info("会话刷新成功 - 会话ID: {}", savedSession.getId());
        
        return savedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserActiveSessions(Long userId) {
        logger.debug("统计用户活跃会话数量 - 用户ID: {}", userId);
        
        return userSessionRepository.countActiveSessionsByUserId(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionInfo> getActiveSessionsByIpAddress(String ipAddress) {
        logger.info("根据IP地址获取活跃会话 - IP: {}", ipAddress);
        
        List<UserSession> sessions = userSessionRepository.findActiveSessionsByIpAddress(ipAddress, LocalDateTime.now());
        
        return sessions.stream()
                .map(this::convertToUserSessionInfo)
                .collect(Collectors.toList());
    }

    @Override
    public void forceLogoutSession(Long sessionId) {
        logger.info("强制注销会话 - 会话ID: {}", sessionId);
        
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));
        
        session.logout();
        userSessionRepository.save(session);
        
        logger.info("会话强制注销成功 - 会话ID: {}", sessionId);
    }

    /**
     * 转换用户会话实体为用户会话信息DTO
     */
    private UserSessionInfo convertToUserSessionInfo(UserSession session) {
        UserSessionInfo sessionInfo = new UserSessionInfo();
        sessionInfo.setId(session.getId());
        sessionInfo.setUserId(session.getUserId());
        
        // 获取用户名
        userRepository.findById(session.getUserId())
                .ifPresent(user -> sessionInfo.setUsername(user.getUsername()));
        
        sessionInfo.setIpAddress(session.getIpAddress());
        sessionInfo.setUserAgent(session.getUserAgent());
        sessionInfo.setDeviceInfo(session.getDeviceInfo());
        sessionInfo.setLocation(session.getLocation());
        sessionInfo.setExpiresAt(session.getExpiresAt());
        sessionInfo.setLastAccessedAt(session.getLastAccessedAt());
        sessionInfo.setIsActive(session.getIsActive());
        sessionInfo.setLogoutAt(session.getLogoutAt());
        sessionInfo.setCreatedAt(session.getCreatedAt());
        
        return sessionInfo;
    }
}