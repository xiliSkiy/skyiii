package com.skyeye.auth.service.impl;

import com.skyeye.auth.entity.OperationLog;
import com.skyeye.auth.repository.OperationLogRepository;
import com.skyeye.auth.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现类
 */
@Service
@Transactional
public class OperationLogServiceImpl implements OperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Override
    public void logOperation(Long userId, String username, String operationType, String operationDesc,
                           String resourceType, String resourceId, String ipAddress, String userAgent) {
        logger.debug("记录操作日志 - 用户: {}, 操作: {}", username, operationType);
        
        OperationLog log = new OperationLog(userId, username, operationType, operationDesc);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.markAsSuccess();
        
        operationLogRepository.save(log);
    }

    @Override
    public void logOperation(Long userId, String username, String operationType, String operationDesc,
                           String resourceType, String resourceId, String ipAddress, String userAgent,
                           String requestData, String responseData, Long executionTime) {
        logger.debug("记录详细操作日志 - 用户: {}, 操作: {}, 执行时间: {}ms", username, operationType, executionTime);
        
        OperationLog log = new OperationLog(userId, username, operationType, operationDesc);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setRequestData(requestData);
        log.setResponseData(responseData);
        log.setExecutionTime(executionTime);
        log.markAsSuccess();
        
        operationLogRepository.save(log);
    }

    @Override
    public void logFailedOperation(Long userId, String username, String operationType, String operationDesc,
                                 String resourceType, String resourceId, String ipAddress, String userAgent,
                                 String errorMessage) {
        logger.warn("记录失败操作日志 - 用户: {}, 操作: {}, 错误: {}", username, operationType, errorMessage);
        
        OperationLog log = new OperationLog(userId, username, operationType, operationDesc);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.markAsFailure(errorMessage);
        
        operationLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OperationLog> getOperationLogs(Long userId, String operationType, String result,
                                             LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        logger.info("查询操作日志 - 用户ID: {}, 操作类型: {}, 结果: {}", userId, operationType, result);
        
        return operationLogRepository.findByConditions(userId, operationType, result, startTime, endTime, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OperationLog> getUserOperationLogs(Long userId, Pageable pageable) {
        logger.info("查询用户操作日志 - 用户ID: {}", userId);
        
        return operationLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OperationLog> getOperationLogsByType(String operationType, Pageable pageable) {
        logger.info("查询操作类型日志 - 操作类型: {}", operationType);
        
        return operationLogRepository.findByOperationTypeOrderByCreatedAtDesc(operationType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OperationLog> getOperationLogsByIpAddress(String ipAddress, Pageable pageable) {
        logger.info("查询IP地址操作日志 - IP: {}", ipAddress);
        
        return operationLogRepository.findByIpAddressOrderByCreatedAtDesc(ipAddress, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OperationLog> getOperationLogsByResource(String resourceType, String resourceId, Pageable pageable) {
        logger.info("查询资源操作日志 - 资源类型: {}, 资源ID: {}", resourceType, resourceId);
        
        return operationLogRepository.findByResource(resourceType, resourceId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserOperations(Long userId, LocalDateTime startTime) {
        logger.debug("统计用户操作次数 - 用户ID: {}, 开始时间: {}", userId, startTime);
        
        return operationLogRepository.countUserOperations(userId, startTime);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFailedOperations(LocalDateTime startTime) {
        logger.debug("统计失败操作次数 - 开始时间: {}", startTime);
        
        return operationLogRepository.countFailedOperations(startTime);
    }

    @Override
    public int cleanupExpiredLogs(int retentionDays) {
        logger.info("清理过期操作日志 - 保留天数: {}", retentionDays);
        
        LocalDateTime expiredBefore = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = operationLogRepository.deleteExpiredLogs(expiredBefore);
        
        logger.info("过期操作日志清理完成 - 删除数量: {}", deletedCount);
        
        return deletedCount;
    }

    @Override
    @Async
    public void logOperationAsync(Long userId, String username, String operationType, String operationDesc,
                                String resourceType, String resourceId, String ipAddress, String userAgent) {
        logger.debug("异步记录操作日志 - 用户: {}, 操作: {}", username, operationType);
        
        try {
            logOperation(userId, username, operationType, operationDesc, 
                        resourceType, resourceId, ipAddress, userAgent);
        } catch (Exception e) {
            logger.error("异步记录操作日志失败", e);
        }
    }
}