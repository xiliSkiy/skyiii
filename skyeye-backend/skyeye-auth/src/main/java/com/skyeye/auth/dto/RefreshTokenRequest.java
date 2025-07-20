package com.skyeye.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

/**
 * 刷新令牌请求DTO
 */
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

    @Schema(description = "刷新令牌", required = true)
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    // Constructors
    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}