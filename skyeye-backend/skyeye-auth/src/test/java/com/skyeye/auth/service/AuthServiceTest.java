package com.skyeye.auth.service;

import com.skyeye.auth.dto.LoginRequest;
import com.skyeye.auth.dto.LoginResponse;
import com.skyeye.auth.dto.RegisterRequest;
import com.skyeye.auth.dto.UserInfo;
import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.enums.UserRole;
import com.skyeye.auth.enums.UserStatus;
import com.skyeye.auth.repository.RoleRepository;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.security.UserPrincipal;
import com.skyeye.auth.service.impl.AuthServiceImpl;
import com.skyeye.auth.util.JwtUtil;
import com.skyeye.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUpService() {
        // Set the jwtExpiration field using reflection
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);
    }

    private User testUser;
    private Role testRole;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // 创建测试角色
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName(UserRole.VIEWER);
        testRole.setDescription("查看者");

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setFullName("测试用户");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setLoginAttempts(0);
        
        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        testUser.setRoles(roles);

        // 创建登录请求
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // 创建注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setFullName("新用户");
    }

    @Test
    void testLoginSuccess() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(createUserPrincipal());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateTokenWithUserId(any(), eq(1L))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertNotNull(response.getUserInfo());
        assertEquals("testuser", response.getUserInfo().getUsername());

        verify(userRepository).save(argThat(user -> 
            user.getLoginAttempts() == 0 && user.getLastLoginAt() != null));
    }

    @Test
    void testLoginFailure() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.login(loginRequest));
        
        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userRepository).save(argThat(user -> user.getLoginAttempts() == 1));
    }

    @Test
    void testRegisterSuccess() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(roleRepository.findByName(UserRole.VIEWER)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserInfo userInfo = authService.register(registerRequest);

        // Then
        assertNotNull(userInfo);
        verify(userRepository).save(argThat(user -> 
            user.getUsername().equals("newuser") && 
            user.getStatus() == UserStatus.ACTIVE));
    }

    @Test
    void testRegisterWithExistingUsername() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.register(registerRequest));
        
        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void testRegisterWithPasswordMismatch() {
        // Given
        registerRequest.setConfirmPassword("differentPassword");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.register(registerRequest));
        
        assertEquals("密码和确认密码不匹配", exception.getMessage());
    }

    @Test
    void testValidateToken() {
        // Given
        String token = "valid-token";
        when(jwtUtil.validateTokenFormat(token)).thenReturn(true);

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateInvalidToken() {
        // Given
        String token = "invalid-token";
        when(jwtUtil.validateTokenFormat(token)).thenThrow(new RuntimeException("Invalid token"));

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertFalse(isValid);
    }

    private UserPrincipal createUserPrincipal() {
        return UserPrincipal.create(testUser);
    }
}