package com.skyeye.auth.service;

import com.skyeye.auth.dto.*;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     */
    UserInfo register(RegisterRequest registerRequest);

    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    /**
     * 用户注销
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     */
    UserInfo getCurrentUser();

    /**
     * 验证令牌
     */
    boolean validateToken(String token);
}