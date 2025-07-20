package com.skyeye.auth.service;

import com.skyeye.auth.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 记录操作日志
     */
    void logOperation(Long userId, String username, String operationType, String operationDesc,
                     String resourceType, String resourceId, String ipAddress, String userAgent);

    /**
     * 记录操作日志（带请求和响应数据）
     */
    void logOperation(Long userId, String username, String operationType, String operationDesc,
                     String resourceType, String resourceId, String ipAddress, String userAgent,
                     String requestData, String responseData, Long executionTime);

    /**
     * 记录失败操作日志
     */
    void logFailedOperation(Long userId, String username, String operationType, String operationDesc,
                           String resourceType, String resourceId, String ipAddress, String userAgent,
                           String errorMessage);

    /**
     * 分页获取操作日志
     */
    Page<OperationLog> getOperationLogs(Long userId, String operationType, String result,
                                       LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据用户ID分页获取操作日志
     */
    Page<OperationLog> getUserOperationLogs(Long userId, Pageable pageable);

    /**
     * 根据操作类型分页获取操作日志
     */
    Page<OperationLog> getOperationLogsByType(String operationType, Pageable pageable);

    /**
     * 根据IP地址分页获取操作日志
     */
    Page<OperationLog> getOperationLogsByIpAddress(String ipAddress, Pageable pageable);

    /**
     * 根据资源分页获取操作日志
     */
    Page<OperationLog> getOperationLogsByResource(String resourceType, String resourceId, Pageable pageable);

    /**
     * 统计用户操作次数
     */
    long countUserOperations(Long userId, LocalDateTime startTime);

    /**
     * 统计失败操作次数
     */
    long countFailedOperations(LocalDateTime startTime);

    /**
     * 清理过期日志
     */
    int cleanupExpiredLogs(int retentionDays);

    /**
     * 异步记录操作日志
     */
    void logOperationAsync(Long userId, String username, String operationType, String operationDesc,
                          String resourceType, String resourceId, String ipAddress, String userAgent);
}