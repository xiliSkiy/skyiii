package com.skyeye.auth.service;

import com.skyeye.auth.dto.PermissionInfo;
import com.skyeye.auth.entity.Permission;
import com.skyeye.auth.entity.User;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 验证用户是否拥有指定权限
     */
    boolean hasPermission(Long userId, String permissionName);

    /**
     * 验证用户是否拥有指定资源的操作权限
     */
    boolean hasPermission(Long userId, String resource, String action);

    /**
     * 获取用户的所有权限
     */
    List<Permission> getUserPermissions(Long userId);

    /**
     * 获取用户的权限名称列表
     */
    List<String> getUserPermissionNames(Long userId);

    /**
     * 获取用户的权限信息列表
     */
    List<PermissionInfo> getUserPermissionInfos(Long userId);

    /**
     * 获取所有权限
     */
    List<PermissionInfo> getAllPermissions();

    /**
     * 获取角色的权限列表
     */
    List<PermissionInfo> getRolePermissions(Long roleId);

    /**
     * 验证用户是否拥有指定角色
     */
    boolean hasRole(Long userId, String roleName);

    /**
     * 刷新用户权限缓存
     */
    void refreshUserPermissions(Long userId);
}