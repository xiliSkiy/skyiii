package com.skyeye.auth.service.impl;

import com.skyeye.auth.dto.PermissionInfo;
import com.skyeye.auth.dto.RoleInfo;
import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.repository.RoleRepository;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.service.RoleService;
import com.skyeye.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleInfo> getAllRoles() {
        logger.info("获取所有角色");
        
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToRoleInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleInfo getRoleById(Long id) {
        logger.info("根据ID获取角色 - ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        
        return convertToRoleInfo(role);
    }

    @Override
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        logger.info("为用户分配角色 - 用户ID: {}, 角色IDs: {}", userId, roleIds);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        Set<Role> rolesToAssign = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new BusinessException("角色不存在: " + roleId));
            
            if (!role.getEnabled()) {
                throw new BusinessException("角色已禁用: " + role.getName());
            }
            
            rolesToAssign.add(role);
        }
        
        // 添加新角色到用户现有角色中
        user.getRoles().addAll(rolesToAssign);
        userRepository.save(user);
        
        logger.info("角色分配成功 - 用户ID: {}, 分配角色数量: {}", userId, roleIds.size());
    }

    @Override
    public void removeRolesFromUser(Long userId, List<Long> roleIds) {
        logger.info("移除用户角色 - 用户ID: {}, 角色IDs: {}", userId, roleIds);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        Set<Role> rolesToRemove = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new BusinessException("角色不存在: " + roleId));
            rolesToRemove.add(role);
        }
        
        // 检查是否要移除系统管理员角色
        boolean removingSystemAdmin = rolesToRemove.stream()
                .anyMatch(role -> role.getName().name().equals("SYSTEM_ADMIN"));
        
        if (removingSystemAdmin) {
            // 检查系统中是否还有其他系统管理员
            long systemAdminCount = userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(userId))
                    .filter(u -> u.getRoles().stream()
                            .anyMatch(r -> r.getName().name().equals("SYSTEM_ADMIN")))
                    .count();
            
            if (systemAdminCount == 0) {
                throw new BusinessException("不能移除最后一个系统管理员的角色");
            }
        }
        
        // 从用户角色中移除指定角色
        user.getRoles().removeAll(rolesToRemove);
        
        // 确保用户至少有一个角色
        if (user.getRoles().isEmpty()) {
            throw new BusinessException("用户必须至少拥有一个角色");
        }
        
        userRepository.save(user);
        
        logger.info("角色移除成功 - 用户ID: {}, 移除角色数量: {}", userId, roleIds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleInfo> getUserRoles(Long userId) {
        logger.info("获取用户角色 - 用户ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return user.getRoles().stream()
                .map(this::convertToRoleInfo)
                .collect(Collectors.toList());
    }

    /**
     * 转换角色实体为角色信息DTO
     */
    private RoleInfo convertToRoleInfo(Role role) {
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setId(role.getId());
        roleInfo.setName(role.getName());
        roleInfo.setDescription(role.getDescription());
        roleInfo.setEnabled(role.getEnabled());
        roleInfo.setCreatedAt(role.getCreatedAt());
        roleInfo.setUpdatedAt(role.getUpdatedAt());
        
        // 设置权限列表
        List<PermissionInfo> permissions = role.getPermissions().stream()
                .map(this::convertToPermissionInfo)
                .collect(Collectors.toList());
        roleInfo.setPermissions(permissions);
        
        return roleInfo;
    }

    /**
     * 转换权限实体为权限信息DTO
     */
    private PermissionInfo convertToPermissionInfo(com.skyeye.auth.entity.Permission permission) {
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