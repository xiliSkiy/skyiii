package com.skyeye.auth.service;

import com.skyeye.auth.dto.CreateUserRequest;
import com.skyeye.auth.dto.UpdateUserRequest;
import com.skyeye.auth.dto.UserInfo;
import com.skyeye.auth.dto.UserListResponse;
import com.skyeye.auth.enums.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取用户列表
     */
    UserListResponse getUsers(String keyword, UserStatus status, Pageable pageable);

    /**
     * 根据ID获取用户
     */
    UserInfo getUserById(Long id);

    /**
     * 创建用户
     */
    UserInfo createUser(CreateUserRequest request);

    /**
     * 更新用户
     */
    UserInfo updateUser(Long id, UpdateUserRequest request);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 批量删除用户
     */
    void deleteUsers(List<Long> userIds);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long id, UserStatus status);

    /**
     * 重置用户密码
     */
    String resetPassword(Long id);

    /**
     * 解锁用户
     */
    void unlockUser(Long id);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}