package com.skyeye.auth.service.impl;

import com.skyeye.auth.dto.PermissionInfo;
import com.skyeye.auth.entity.Permission;
import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.enums.UserRole;
import com.skyeye.auth.repository.PermissionRepository;
import com.skyeye.auth.repository.RoleRepository;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.service.PermissionService;
import com.skyeye.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId + ':' + #permissionName")
    public boolean hasPermission(Long userId, String permissionName) {
        logger.debug("检查用户 {} 是否拥有权限: {}", userId, permissionName);

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            logger.warn("用户不存在: {}", userId);
            return false;
        }

        User user = userOpt.get();
        
        // 检查用户是否激活
        if (!user.isActive()) {
            logger.warn("用户 {} 未激活或被锁定", userId);
            return false;
        }

        // 系统管理员拥有所有权限
        if (user.hasRole(UserRole.SYSTEM_ADMIN)) {
            logger.debug("用户 {} 是系统管理员，拥有所有权限", userId);
            return true;
        }

        // 检查用户角色是否拥有该权限
        boolean hasPermission = user.getRoles().stream()
                .filter(Role::getEnabled)
                .flatMap(role -> role.getPermissions().stream())
                .filter(Permission::getEnabled)
                .anyMatch(permission -> permission.getName().equals(permissionName));

        logger.debug("用户 {} 权限 {} 检查结果: {}", userId, permissionName, hasPermission);
        return hasPermission;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId + ':' + #resource + ':' + #action")
    public boolean hasPermission(Long userId, String resource, String action) {
        logger.debug("检查用户 {} 是否拥有资源 {} 的 {} 权限", userId, resource, action);

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            logger.warn("用户不存在: {}", userId);
            return false;
        }

        User user = userOpt.get();
        
        // 检查用户是否激活
        if (!user.isActive()) {
            logger.warn("用户 {} 未激活或被锁定", userId);
            return false;
        }

        // 系统管理员拥有所有权限
        if (user.hasRole(UserRole.SYSTEM_ADMIN)) {
            logger.debug("用户 {} 是系统管理员，拥有所有权限", userId);
            return true;
        }

        // 检查用户角色是否拥有该资源的操作权限
        boolean hasPermission = user.getRoles().stream()
                .filter(Role::getEnabled)
                .flatMap(role -> role.getPermissions().stream())
                .filter(Permission::getEnabled)
                .anyMatch(permission -> 
                    resource.equals(permission.getResource()) && 
                    action.equals(permission.getAction()));

        logger.debug("用户 {} 资源 {} 操作 {} 权限检查结果: {}", userId, resource, action, hasPermission);
        return hasPermission;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId + ':permissions'")
    public List<Permission> getUserPermissions(Long userId) {
        logger.debug("获取用户 {} 的所有权限", userId);
        return permissionRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId + ':permissionNames'")
    public List<String> getUserPermissionNames(Long userId) {
        logger.debug("获取用户 {} 的权限名称列表", userId);
        return getUserPermissions(userId).stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId + ':role:' + #roleName")
    public boolean hasRole(Long userId, String roleName) {
        logger.debug("检查用户 {} 是否拥有角色: {}", userId, roleName);

        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            logger.warn("用户不存在: {}", userId);
            return false;
        }

        User user = userOpt.get();
        
        // 检查用户是否激活
        if (!user.isActive()) {
            logger.warn("用户 {} 未激活或被锁定", userId);
            return false;
        }

        try {
            UserRole role = UserRole.valueOf(roleName);
            boolean hasRole = user.hasRole(role);
            logger.debug("用户 {} 角色 {} 检查结果: {}", userId, roleName, hasRole);
            return hasRole;
        } catch (IllegalArgumentException e) {
            logger.warn("无效的角色名称: {}", roleName);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userPermissions", key = "#userId + ':permissionInfos'")
    public List<PermissionInfo> getUserPermissionInfos(Long userId) {
        logger.debug("获取用户 {} 的权限信息列表", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return user.getRoles().stream()
                .filter(Role::getEnabled)
                .flatMap(role -> role.getPermissions().stream())
                .filter(Permission::getEnabled)
                .distinct()
                .map(this::convertToPermissionInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "permissions", key = "'all'")
    public List<PermissionInfo> getAllPermissions() {
        logger.debug("获取所有权限");
        
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(this::convertToPermissionInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "rolePermissions", key = "#roleId")
    public List<PermissionInfo> getRolePermissions(Long roleId) {
        logger.debug("获取角色 {} 的权限列表", roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        
        return role.getPermissions().stream()
                .filter(Permission::getEnabled)
                .map(this::convertToPermissionInfo)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "userPermissions", key = "#userId + '*'")
    public void refreshUserPermissions(Long userId) {
        logger.info("刷新用户 {} 的权限缓存", userId);
        // 缓存会被自动清除，下次访问时重新加载
    }

    /**
     * 转换权限实体为权限信息DTO
     */
    private PermissionInfo convertToPermissionInfo(Permission permission) {
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.setId(permission.getId());
        permissionInfo.setName(permission.getName());
        permissionInfo.setDescription(permission.getDescription());
        permissionInfo.setResource(permission.getResource());
        permissionInfo.setAction(permission.getAction());
        permissionInfo.setEnabled(permission.getEnabled());
        permissionInfo.setCreatedAt(permission.getCreatedAt());
        permissionInfo.setUpdatedAt(permission.getUpdatedAt());
        return permissionInfo;
    }
}