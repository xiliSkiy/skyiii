package com.skyeye.auth.service;

import com.skyeye.auth.dto.RoleInfo;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 获取所有角色
     */
    List<RoleInfo> getAllRoles();

    /**
     * 根据ID获取角色
     */
    RoleInfo getRoleById(Long id);

    /**
     * 为用户分配角色
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 移除用户角色
     */
    void removeRolesFromUser(Long userId, List<Long> roleIds);

    /**
     * 获取用户角色
     */
    List<RoleInfo> getUserRoles(Long userId);
}