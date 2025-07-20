package com.skyeye.auth.controller;

import com.skyeye.auth.dto.PermissionInfo;
import com.skyeye.auth.service.PermissionService;
import com.skyeye.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理", description = "权限管理相关接口")
@RestController
@RequestMapping("/api/v1/permissions")
@PreAuthorize("hasAuthority('role:read')")
public class PermissionController {

    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

    @Autowired
    private PermissionService permissionService;

    @Operation(summary = "获取所有权限", description = "获取系统中所有权限列表")
    @GetMapping
    public ApiResponse<List<PermissionInfo>> getAllPermissions() {
        logger.info("获取所有权限请求");
        List<PermissionInfo> permissions = permissionService.getAllPermissions();
        return ApiResponse.success("获取权限列表成功", permissions);
    }

    @Operation(summary = "获取用户权限", description = "获取指定用户的所有权限")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<PermissionInfo>> getUserPermissions(@Parameter(description = "用户ID") @PathVariable Long userId) {
        logger.info("获取用户权限请求 - 用户ID: {}", userId);
        List<PermissionInfo> permissions = permissionService.getUserPermissionInfos(userId);
        return ApiResponse.success("获取用户权限成功", permissions);
    }

    @Operation(summary = "获取角色权限", description = "获取指定角色的所有权限")
    @GetMapping("/role/{roleId}")
    public ApiResponse<List<PermissionInfo>> getRolePermissions(@Parameter(description = "角色ID") @PathVariable Long roleId) {
        logger.info("获取角色权限请求 - 角色ID: {}", roleId);
        List<PermissionInfo> permissions = permissionService.getRolePermissions(roleId);
        return ApiResponse.success("获取角色权限成功", permissions);
    }

    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    @GetMapping("/check")
    public ApiResponse<Boolean> checkPermission(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "权限名称") @RequestParam String permission) {
        logger.info("检查用户权限请求 - 用户ID: {}, 权限: {}", userId, permission);
        boolean hasPermission = permissionService.hasPermission(userId, permission);
        return ApiResponse.success("权限检查完成", hasPermission);
    }
}