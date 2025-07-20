package com.skyeye.auth.controller;

import com.skyeye.auth.dto.*;
import com.skyeye.auth.service.AuthService;
import com.skyeye.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("用户登录请求: {}", loginRequest.getUsername());
        LoginResponse response = authService.login(loginRequest);
        return ApiResponse.success("登录成功", response);
    }

    @Operation(summary = "用户注册", description = "注册新用户账户")
    @PostMapping("/register")
    public ApiResponse<UserInfo> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("用户注册请求: {}", registerRequest.getUsername());
        UserInfo userInfo = authService.register(registerRequest);
        return ApiResponse.success("注册成功", userInfo);
    }

    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        logger.info("刷新令牌请求");
        LoginResponse response = authService.refreshToken(refreshTokenRequest);
        return ApiResponse.success("令牌刷新成功", response);
    }

    @Operation(summary = "用户注销", description = "用户注销登录")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        authService.logout(token);
        logger.info("用户注销成功");
        return ApiResponse.success("注销成功", "注销成功");
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserInfo> getCurrentUser() {
        UserInfo userInfo = authService.getCurrentUser();
        return ApiResponse.success("获取用户信息成功", userInfo);
    }

    @Operation(summary = "验证令牌", description = "验证访问令牌是否有效")
    @PostMapping("/validate")
    public ApiResponse<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ApiResponse.success("令牌验证完成", isValid);
    }

    /**
     * 从请求中获取JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}