package com.skyeye.auth.repository;

import com.skyeye.auth.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志数据访问接口
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    /**
     * 根据用户ID分页查找操作日志
     */
    Page<OperationLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据操作类型分页查找操作日志
     */
    Page<OperationLog> findByOperationTypeOrderByCreatedAtDesc(String operationType, Pageable pageable);

    /**
     * 根据用户ID和操作类型分页查找操作日志
     */
    Page<OperationLog> findByUserIdAndOperationTypeOrderByCreatedAtDesc(Long userId, String operationType, Pageable pageable);

    /**
     * 根据时间范围查找操作日志
     */
    @Query("SELECT l FROM OperationLog l WHERE l.createdAt BETWEEN :startTime AND :endTime ORDER BY l.createdAt DESC")
    Page<OperationLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime, 
                                       Pageable pageable);

    /**
     * 根据多个条件查找操作日志
     */
    @Query("SELECT l FROM OperationLog l WHERE " +
           "(:userId IS NULL OR l.userId = :userId) AND " +
           "(:operationType IS NULL OR l.operationType = :operationType) AND " +
           "(:result IS NULL OR l.result = :result) AND " +
           "(:startTime IS NULL OR l.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR l.createdAt <= :endTime) " +
           "ORDER BY l.createdAt DESC")
    Page<OperationLog> findByConditions(@Param("userId") Long userId,
                                        @Param("operationType") String operationType,
                                        @Param("result") String result,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        Pageable pageable);

    /**
     * 根据IP地址查找操作日志
     */
    Page<OperationLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);

    /**
     * 统计用户操作次数
     */
    @Query("SELECT COUNT(l) FROM OperationLog l WHERE l.userId = :userId AND l.createdAt >= :startTime")
    long countUserOperations(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 统计操作类型分布
     */
    @Query("SELECT l.operationType, COUNT(l) FROM OperationLog l WHERE l.createdAt >= :startTime GROUP BY l.operationType")
    List<Object[]> countOperationsByType(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计失败操作
     */
    @Query("SELECT COUNT(l) FROM OperationLog l WHERE l.result = 'FAILURE' AND l.createdAt >= :startTime")
    long countFailedOperations(@Param("startTime") LocalDateTime startTime);

    /**
     * 删除过期日志
     */
    @Modifying
    @Query("DELETE FROM OperationLog l WHERE l.createdAt <= :expiredBefore")
    int deleteExpiredLogs(@Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * 根据资源类型和资源ID查找操作日志
     */
    @Query("SELECT l FROM OperationLog l WHERE l.resourceType = :resourceType AND l.resourceId = :resourceId ORDER BY l.createdAt DESC")
    Page<OperationLog> findByResource(@Param("resourceType") String resourceType, 
                                      @Param("resourceId") String resourceId, 
                                      Pageable pageable);
}