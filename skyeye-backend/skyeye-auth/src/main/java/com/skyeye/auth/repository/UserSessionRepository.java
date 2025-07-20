package com.skyeye.auth.repository;

import com.skyeye.auth.entity.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户会话数据访问接口
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * 根据会话令牌查找会话
     */
    Optional<UserSession> findBySessionToken(String sessionToken);

    /**
     * 根据刷新令牌查找会话
     */
    Optional<UserSession> findByRefreshToken(String refreshToken);

    /**
     * 根据用户ID查找活跃会话
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.isActive = true AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 根据用户ID分页查找会话
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId ORDER BY s.createdAt DESC")
    Page<UserSession> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找所有活跃会话
     */
    @Query("SELECT s FROM UserSession s WHERE s.isActive = true AND s.expiresAt > :now")
    List<UserSession> findActiveSessions(@Param("now") LocalDateTime now);

    /**
     * 查找过期的会话
     */
    @Query("SELECT s FROM UserSession s WHERE s.isActive = true AND s.expiresAt <= :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * 批量注销用户的所有会话
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.logoutAt = :logoutTime WHERE s.userId = :userId AND s.isActive = true")
    int logoutAllUserSessions(@Param("userId") Long userId, @Param("logoutTime") LocalDateTime logoutTime);

    /**
     * 批量注销过期会话
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.logoutAt = :logoutTime WHERE s.isActive = true AND s.expiresAt <= :now")
    int logoutExpiredSessions(@Param("now") LocalDateTime now, @Param("logoutTime") LocalDateTime logoutTime);

    /**
     * 删除过期会话
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt <= :expiredBefore")
    int deleteExpiredSessions(@Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * 统计用户活跃会话数量
     */
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.isActive = true AND s.expiresAt > :now")
    long countActiveSessionsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 根据IP地址查找会话
     */
    @Query("SELECT s FROM UserSession s WHERE s.ipAddress = :ipAddress AND s.isActive = true AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByIpAddress(@Param("ipAddress") String ipAddress, @Param("now") LocalDateTime now);
}