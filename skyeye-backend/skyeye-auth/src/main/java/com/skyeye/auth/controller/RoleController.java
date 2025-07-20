package com.skyeye.auth.controller;

import com.skyeye.auth.dto.AssignRoleRequest;
import com.skyeye.auth.dto.RoleInfo;
import com.skyeye.auth.service.RoleService;
import com.skyeye.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/api/v1/roles")
@PreAuthorize("hasAuthority('role:read')")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    @Operation(summary = "获取所有角色", description = "获取系统中所有角色列表")
    @GetMapping
    public ApiResponse<List<RoleInfo>> getAllRoles() {
        logger.info("获取所有角色请求");
        List<RoleInfo> roles = roleService.getAllRoles();
        return ApiResponse.success("获取角色列表成功", roles);
    }

    @Operation(summary = "根据ID获取角色", description = "根据角色ID获取角色详细信息")
    @GetMapping("/{id}")
    public ApiResponse<RoleInfo> getRoleById(@Parameter(description = "角色ID") @PathVariable Long id) {
        logger.info("获取角色详情请求 - ID: {}", id);
        RoleInfo roleInfo = roleService.getRoleById(id);
        return ApiResponse.success("获取角色信息成功", roleInfo);
    }

    @Operation(summary = "为用户分配角色", description = "为指定用户分配角色")
    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('role:manage')")
    public ApiResponse<String> assignRolesToUser(@Valid @RequestBody AssignRoleRequest request) {
        logger.info("分配角色请求 - 用户ID: {}, 角色IDs: {}", request.getUserId(), request.getRoleIds());
        roleService.assignRolesToUser(request.getUserId(), request.getRoleIds());
        return ApiResponse.success("分配角色成功", "角色已分配");
    }

    @Operation(summary = "移除用户角色", description = "移除用户的指定角色")
    @DeleteMapping("/remove")
    @PreAuthorize("hasAuthority('role:manage')")
    public ApiResponse<String> removeRolesFromUser(@Valid @RequestBody AssignRoleRequest request) {
        logger.info("移除角色请求 - 用户ID: {}, 角色IDs: {}", request.getUserId(), request.getRoleIds());
        roleService.removeRolesFromUser(request.getUserId(), request.getRoleIds());
        return ApiResponse.success("移除角色成功", "角色已移除");
    }

    @Operation(summary = "获取用户角色", description = "获取指定用户的所有角色")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<RoleInfo>> getUserRoles(@Parameter(description = "用户ID") @PathVariable Long userId) {
        logger.info("获取用户角色请求 - 用户ID: {}", userId);
        List<RoleInfo> roles = roleService.getUserRoles(userId);
        return ApiResponse.success("获取用户角色成功", roles);
    }
}