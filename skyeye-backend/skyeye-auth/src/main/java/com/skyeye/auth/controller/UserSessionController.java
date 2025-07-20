package com.skyeye.auth.controller;

import com.skyeye.auth.dto.UserSessionInfo;
import com.skyeye.auth.service.UserSessionService;
import com.skyeye.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户会话管理控制器
 */
@Tag(name = "用户会话管理", description = "用户会话管理相关接口")
@RestController
@RequestMapping("/api/v1/user-sessions")
@PreAuthorize("hasAuthority('session:read')")
public class UserSessionController {

    private static final Logger logger = LoggerFactory.getLogger(UserSessionController.class);

    @Autowired
    private UserSessionService userSessionService;

    @Operation(summary = "获取用户活跃会话", description = "获取指定用户的所有活跃会话")
    @GetMapping("/user/{userId}/active")
    public ApiResponse<List<UserSessionInfo>> getUserActiveSessions(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        logger.info("获取用户活跃会话请求 - 用户ID: {}", userId);
        
        List<UserSessionInfo> sessions = userSessionService.getUserActiveSessions(userId);
        return ApiResponse.success("获取用户活跃会话成功", sessions);
    }

    @Operation(summary = "获取用户会话历史", description = "分页获取用户会话历史记录")
    @GetMapping("/user/{userId}/history")
    public ApiResponse<Page<UserSessionInfo>> getUserSessionHistory(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("获取用户会话历史请求 - 用户ID: {}, 页码: {}, 大小: {}", userId, page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserSessionInfo> sessionPage = userSessionService.getUserSessionHistory(userId, pageable);
        return ApiResponse.success("获取用户会话历史成功", sessionPage);
    }

    @Operation(summary = "获取所有活跃会话", description = "获取系统中所有活跃会话")
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('session:manage')")
    public ApiResponse<List<UserSessionInfo>> getAllActiveSessions() {
        logger.info("获取所有活跃会话请求");
        
        List<UserSessionInfo> sessions = userSessionService.getAllActiveSessions();
        return ApiResponse.success("获取所有活跃会话成功", sessions);
    }

    @Operation(summary = "注销用户所有会话", description = "注销指定用户的所有会话")
    @PostMapping("/user/{userId}/logout-all")
    @PreAuthorize("hasAuthority('session:manage')")
    public ApiResponse<String> logoutAllUserSessions(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        logger.info("注销用户所有会话请求 - 用户ID: {}", userId);
        
        userSessionService.logoutAllUserSessions(userId);
        return ApiResponse.success("注销用户所有会话成功", "所有会话已注销");
    }

    @Operation(summary = "强制注销会话", description = "强制注销指定会话")
    @PostMapping("/{sessionId}/force-logout")
    @PreAuthorize("hasAuthority('session:manage')")
    public ApiResponse<String> forceLogoutSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        logger.info("强制注销会话请求 - 会话ID: {}", sessionId);
        
        userSessionService.forceLogoutSession(sessionId);
        return ApiResponse.success("强制注销会话成功", "会话已注销");
    }

    @Operation(summary = "统计用户活跃会话数量", description = "统计指定用户的活跃会话数量")
    @GetMapping("/user/{userId}/count")
    public ApiResponse<Long> countUserActiveSessions(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        logger.info("统计用户活跃会话数量请求 - 用户ID: {}", userId);
        
        long count = userSessionService.countUserActiveSessions(userId);
        return ApiResponse.success("统计用户活跃会话数量成功", count);
    }

    @Operation(summary = "根据IP获取活跃会话", description = "根据IP地址获取活跃会话")
    @GetMapping("/ip/{ipAddress}")
    @PreAuthorize("hasAuthority('session:manage')")
    public ApiResponse<List<UserSessionInfo>> getActiveSessionsByIpAddress(
            @Parameter(description = "IP地址") @PathVariable String ipAddress) {
        logger.info("根据IP获取活跃会话请求 - IP: {}", ipAddress);
        
        List<UserSessionInfo> sessions = userSessionService.getActiveSessionsByIpAddress(ipAddress);
        return ApiResponse.success("根据IP获取活跃会话成功", sessions);
    }

    @Operation(summary = "清理过期会话", description = "清理系统中的过期会话")
    @PostMapping("/cleanup")
    @PreAuthorize("hasAuthority('session:manage')")
    public ApiResponse<Integer> cleanupExpiredSessions() {
        logger.info("清理过期会话请求");
        
        int cleanedCount = userSessionService.cleanupExpiredSessions();
        return ApiResponse.success("清理过期会话成功", cleanedCount);
    }

    @Operation(summary = "验证会话有效性", description = "验证指定会话令牌是否有效")
    @GetMapping("/validate")
    public ApiResponse<Boolean> validateSession(
            @Parameter(description = "会话令牌") @RequestParam String sessionToken) {
        logger.debug("验证会话有效性请求");
        
        boolean isValid = userSessionService.isSessionValid(sessionToken);
        return ApiResponse.success("会话验证完成", isValid);
    }
}