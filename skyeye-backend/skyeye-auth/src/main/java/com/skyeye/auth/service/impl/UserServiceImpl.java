package com.skyeye.auth.service.impl;

import com.skyeye.auth.dto.CreateUserRequest;
import com.skyeye.auth.dto.UpdateUserRequest;
import com.skyeye.auth.dto.UserInfo;
import com.skyeye.auth.dto.UserListResponse;
import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.enums.UserRole;
import com.skyeye.auth.enums.UserStatus;
import com.skyeye.auth.repository.RoleRepository;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.service.UserService;
import com.skyeye.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String DEFAULT_PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_PASSWORD_LENGTH = 8;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserListResponse getUsers(String keyword, UserStatus status, Pageable pageable) {
        logger.info("获取用户列表 - 关键词: {}, 状态: {}, 分页: {}", keyword, status, pageable);
        
        Page<User> userPage = userRepository.findByKeywordAndStatus(keyword, status, pageable);
        
        List<UserInfo> userInfos = userPage.getContent().stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
        
        return new UserListResponse(
                userInfos,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getNumber(),
                userPage.getSize(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfo getUserById(Long id) {
        logger.info("根据ID获取用户 - ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return convertToUserInfo(user);
    }

    @Override
    public UserInfo createUser(CreateUserRequest request) {
        logger.info("创建用户 - 用户名: {}", request.getUsername());
        
        // 验证用户名和邮箱唯一性
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已存在");
        }
        
        // 创建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);
        
        // 设置角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException("角色不存在: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        } else {
            // 默认分配普通用户角色
            Role defaultRole = roleRepository.findByName(UserRole.VIEWER)
                    .orElseThrow(() -> new BusinessException("默认角色不存在"));
            user.getRoles().add(defaultRole);
        }
        
        User savedUser = userRepository.save(user);
        logger.info("用户创建成功 - ID: {}, 用户名: {}", savedUser.getId(), savedUser.getUsername());
        
        return convertToUserInfo(savedUser);
    }

    @Override
    public UserInfo updateUser(Long id, UpdateUserRequest request) {
        logger.info("更新用户 - ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 更新邮箱（检查唯一性）
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已存在");
            }
            user.setEmail(request.getEmail());
        }
        
        // 更新其他字段
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        // 更新角色
        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException("角色不存在: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }
        
        User savedUser = userRepository.save(user);
        logger.info("用户更新成功 - ID: {}", savedUser.getId());
        
        return convertToUserInfo(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("删除用户 - ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查是否为系统管理员
        if (user.hasRole(UserRole.SYSTEM_ADMIN)) {
            throw new BusinessException("不能删除系统管理员");
        }
        
        userRepository.delete(user);
        logger.info("用户删除成功 - ID: {}", id);
    }

    @Override
    public void deleteUsers(List<Long> userIds) {
        logger.info("批量删除用户 - IDs: {}", userIds);
        
        for (Long userId : userIds) {
            deleteUser(userId);
        }
        
        logger.info("批量删除用户完成 - 数量: {}", userIds.size());
    }

    @Override
    public void updateUserStatus(Long id, UserStatus status) {
        logger.info("更新用户状态 - ID: {}, 状态: {}", id, status);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查是否为系统管理员
        if (user.hasRole(UserRole.SYSTEM_ADMIN) && status != UserStatus.ACTIVE) {
            throw new BusinessException("不能禁用系统管理员");
        }
        
        user.setStatus(status);
        
        // 如果是锁定状态，清除锁定时间
        if (status != UserStatus.LOCKED) {
            user.setLockedUntil(null);
            user.setLoginAttempts(0);
        }
        
        userRepository.save(user);
        logger.info("用户状态更新成功 - ID: {}, 状态: {}", id, status);
    }

    @Override
    public String resetPassword(Long id) {
        logger.info("重置用户密码 - ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 生成随机密码
        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // 重置登录尝试次数和锁定状态
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        
        userRepository.save(user);
        logger.info("用户密码重置成功 - ID: {}", id);
        
        return newPassword;
    }

    @Override
    public void unlockUser(Long id) {
        logger.info("解锁用户 - ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        user.setLockedUntil(null);
        user.setLoginAttempts(0);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        
        userRepository.save(user);
        logger.info("用户解锁成功 - ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 转换用户实体为用户信息DTO
     */
    private UserInfo convertToUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setFullName(user.getFullName());
        userInfo.setPhone(user.getPhone());
        userInfo.setStatus(user.getStatus());
        userInfo.setLastLoginAt(user.getLastLoginAt());
        
        // 设置角色
        Set<UserRole> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        userInfo.setRoles(roles);
        
        // 设置权限
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .filter(permission -> permission.getEnabled())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());
        userInfo.setPermissions(permissions);
        
        return userInfo;
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < DEFAULT_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(DEFAULT_PASSWORD_CHARS.length());
            password.append(DEFAULT_PASSWORD_CHARS.charAt(index));
        }
        
        return password.toString();
    }
}