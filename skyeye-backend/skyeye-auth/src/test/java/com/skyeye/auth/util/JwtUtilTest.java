package com.skyeye.auth.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // 设置测试配置
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-jwt-token-generation-and-validation-in-test-environment-with-sufficient-length-for-hs512-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24小时
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L); // 7天

        // 创建测试用户
        userDetails = User.builder()
            .username("testuser")
            .password("password")
            .authorities(new ArrayList<>())
            .build();
    }

    @Test
    void testGenerateToken() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT应该有3个部分
    }

    @Test
    void testGenerateRefreshToken() {
        // When
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(jwtUtil.isRefreshToken(refreshToken));
    }

    @Test
    void testGetUsernameFromToken() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String username = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void testValidateToken() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenWithWrongUser() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        UserDetails wrongUser = User.builder()
            .username("wronguser")
            .password("password")
            .authorities(new ArrayList<>())
            .build();

        // When
        Boolean isValid = jwtUtil.validateToken(token, wrongUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsTokenExpired() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testValidateTokenFormat() {
        // Given
        String validToken = jwtUtil.generateToken(userDetails);
        String invalidToken = "invalid.token.format";

        // When & Then
        assertTrue(jwtUtil.validateTokenFormat(validToken));
        assertFalse(jwtUtil.validateTokenFormat(invalidToken));
    }

    @Test
    void testGenerateTokenWithUserId() {
        // Given
        Long userId = 123L;

        // When
        String token = jwtUtil.generateTokenWithUserId(userDetails, userId);

        // Then
        assertNotNull(token);
        assertEquals(userId, jwtUtil.getUserIdFromToken(token));
    }

    @Test
    void testIsRefreshToken() {
        // Given
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // When & Then
        assertFalse(jwtUtil.isRefreshToken(accessToken));
        assertTrue(jwtUtil.isRefreshToken(refreshToken));
    }
}