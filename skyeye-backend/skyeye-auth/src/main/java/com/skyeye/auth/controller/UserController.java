package com.skyeye.auth.controller;

import com.skyeye.auth.dto.CreateUserRequest;
import com.skyeye.auth.dto.UpdateUserRequest;
import com.skyeye.auth.dto.UserInfo;
import com.skyeye.auth.dto.UserListResponse;
import com.skyeye.auth.enums.UserStatus;
import com.skyeye.auth.service.UserService;
import com.skyeye.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAuthority('user:read')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持关键词搜索和状态筛选")
    @GetMapping
    public ApiResponse<UserListResponse> getUsers(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户状态") @RequestParam(required = false) UserStatus status,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.info("获取用户列表请求 - 页码: {}, 大小: {}, 关键词: {}, 状态: {}", page, size, keyword, status);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        UserListResponse response = userService.getUsers(keyword, status, pageable);
        return ApiResponse.success("获取用户列表成功", response);
    }

    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public ApiResponse<UserInfo> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        logger.info("获取用户详情请求 - ID: {}", id);
        UserInfo userInfo = userService.getUserById(id);
        return ApiResponse.success("获取用户信息成功", userInfo);
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ApiResponse<UserInfo> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("创建用户请求 - 用户名: {}", request.getUsername());
        UserInfo userInfo = userService.createUser(request);
        return ApiResponse.success("创建用户成功", userInfo);
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<UserInfo> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        logger.info("更新用户请求 - ID: {}", id);
        UserInfo userInfo = userService.updateUser(id, request);
        return ApiResponse.success("更新用户成功", userInfo);
    }

    @Operation(summary = "删除用户", description = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ApiResponse<String> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        logger.info("删除用户请求 - ID: {}", id);
        userService.deleteUser(id);
        return ApiResponse.success("删除用户成功", "用户已删除");
    }

    @Operation(summary = "批量删除用户", description = "批量删除用户")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('user:delete')")
    public ApiResponse<String> deleteUsers(@RequestBody List<Long> userIds) {
        logger.info("批量删除用户请求 - IDs: {}", userIds);
        userService.deleteUsers(userIds);
        return ApiResponse.success("批量删除用户成功", "用户已删除");
    }

    @Operation(summary = "激活用户", description = "激活用户账户")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<String> activateUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        logger.info("激活用户请求 - ID: {}", id);
        userService.updateUserStatus(id, UserStatus.ACTIVE);
        return ApiResponse.success("激活用户成功", "用户已激活");
    }

    @Operation(summary = "锁定用户", description = "锁定用户账户")
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<String> lockUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        logger.info("锁定用户请求 - ID: {}", id);
        userService.updateUserStatus(id, UserStatus.LOCKED);
        return ApiResponse.success("锁定用户成功", "用户已锁定");
    }

    @Operation(summary = "禁用用户", description = "禁用用户账户")
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<String> disableUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        logger.info("禁用用户请求 - ID: {}", id);
        userService.updateUserStatus(id, UserStatus.DISABLED);
        return ApiResponse.success("禁用用户成功", "用户已禁用");
    }

    @Operation(summary = "重置用户密码", description = "重置用户密码")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<String> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id) {
        logger.info("重置用户密码请求 - ID: {}", id);
        String newPassword = userService.resetPassword(id);
        return ApiResponse.success("重置密码成功", "新密码: " + newPassword);
    }

    @Operation(summary = "解锁用户", description = "解锁被锁定的用户")
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<String> unlockUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        logger.info("解锁用户请求 - ID: {}", id);
        userService.unlockUser(id);
        return ApiResponse.success("解锁用户成功", "用户已解锁");
    }
}