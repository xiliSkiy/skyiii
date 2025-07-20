package com.skyeye.auth.repository;

import com.skyeye.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限数据访问接口
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限名称查找权限
     */
    Optional<Permission> findByName(String name);

    /**
     * 根据资源和操作查找权限
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);

    /**
     * 检查权限名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 查找启用的权限
     */
    List<Permission> findByEnabledTrue();

    /**
     * 根据资源查找权限
     */
    List<Permission> findByResource(String resource);

    /**
     * 根据用户ID查找用户拥有的权限
     */
    @Query("SELECT DISTINCT p FROM User u " +
           "JOIN u.roles r " +
           "JOIN r.permissions p " +
           "WHERE u.id = :userId AND p.enabled = true")
    List<Permission> findByUserId(@Param("userId") Long userId);
}