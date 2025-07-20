package com.skyeye.auth.repository;

import com.skyeye.auth.entity.User;
import com.skyeye.auth.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据状态查找用户
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * 根据用户名或邮箱模糊查询
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.fullName LIKE %:keyword%) " +
           "AND (:status IS NULL OR u.status = :status)")
    Page<User> findByKeywordAndStatus(@Param("keyword") String keyword, 
                                     @Param("status") UserStatus status, 
                                     Pageable pageable);

    /**
     * 查找需要解锁的用户
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now")
    Page<User> findUsersToUnlock(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 统计用户数量按状态
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    Object[] countByStatus();
}