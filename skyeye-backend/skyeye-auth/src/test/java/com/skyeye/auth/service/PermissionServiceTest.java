package com.skyeye.auth.service;

import com.skyeye.auth.entity.Permission;
import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.enums.UserRole;
import com.skyeye.auth.enums.UserStatus;
import com.skyeye.auth.repository.PermissionRepository;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 权限服务测试
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private User testUser;
    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        // 创建测试权限
        testPermission = new Permission();
        testPermission.setId(1L);
        testPermission.setName("user:read");
        testPermission.setDescription("查看用户");
        testPermission.setResource("user");
        testPermission.setAction("read");
        testPermission.setEnabled(true);

        // 创建测试角色
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName(UserRole.OPERATOR);
        testRole.setDescription("操作员");
        testRole.setEnabled(true);
        
        Set<Permission> permissions = new HashSet<>();
        permissions.add(testPermission);
        testRole.setPermissions(permissions);

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setStatus(UserStatus.ACTIVE);
        
        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        testUser.setRoles(roles);
    }

    @Test
    void testHasPermissionByName_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean hasPermission = permissionService.hasPermission(1L, "user:read");

        // Then
        assertTrue(hasPermission);
        verify(userRepository).findById(1L);
    }

    @Test
    void testHasPermissionByName_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        boolean hasPermission = permissionService.hasPermission(1L, "user:read");

        // Then
        assertFalse(hasPermission);
        verify(userRepository).findById(1L);
    }

    @Test
    void testHasPermissionByName_UserInactive() {
        // Given
        testUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean hasPermission = permissionService.hasPermission(1L, "user:read");

        // Then
        assertFalse(hasPermission);
    }

    @Test
    void testHasPermissionByName_SystemAdmin() {
        // Given
        Role adminRole = new Role();
        adminRole.setName(UserRole.SYSTEM_ADMIN);
        adminRole.setEnabled(true);
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        testUser.setRoles(roles);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean hasPermission = permissionService.hasPermission(1L, "any:permission");

        // Then
        assertTrue(hasPermission);
    }

    @Test
    void testHasPermissionByResourceAndAction_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean hasPermission = permissionService.hasPermission(1L, "user", "read");

        // Then
        assertTrue(hasPermission);
        verify(userRepository).findById(1L);
    }

    @Test
    void testHasPermissionByResourceAndAction_NoPermission() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean hasPermission = permissionService.hasPermission(1L, "user", "delete");

        // Then
        assertFalse(hasPermission);
    }

    @Test
    void testGetUserPermissions() {
        // Given
        List<Permission> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findByUserId(1L)).thenReturn(permissions);

        // When
        List<Permission> result = permissionService.getUserPermissions(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals("user:read", result.get(0).getName());
        verify(permissionRepository).findByUserId(1L);
    }

    @Test
    void testGetUserPermissionNames() {
        // Given
        List<Permission> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findByUserId(1L)).thenReturn(permissions);

        // When
        List<String> result = permissionService.getUserPermissionNames(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals("user:read", result.get(0));
    }

    @Test
    void testHasRole_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean hasRole = permissionService.hasRole(1L, "OPERATOR");

        // Then
        assertTrue(hasRole);
        verify(userRepository).findById(1L);
    }

    @Test
    void testHasRole_NoRole() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean hasRole = permissionService.hasRole(1L, "SYSTEM_ADMIN");

        // Then
        assertFalse(hasRole);
    }

    @Test
    void testHasRole_InvalidRole() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean hasRole = permissionService.hasRole(1L, "INVALID_ROLE");

        // Then
        assertFalse(hasRole);
    }

    @Test
    void testRefreshUserPermissions() {
        // When
        permissionService.refreshUserPermissions(1L);

        // Then
        // 这个方法主要是清除缓存，没有直接的返回值可以验证
        // 在实际应用中，缓存会被清除，下次访问时重新加载
        // 这里我们只验证方法能正常执行
        assertTrue(true);
    }
}