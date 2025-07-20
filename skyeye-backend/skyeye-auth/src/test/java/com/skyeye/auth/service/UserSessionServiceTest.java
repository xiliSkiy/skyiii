package com.skyeye.auth.service;

import com.skyeye.auth.dto.UserSessionInfo;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.entity.UserSession;
import com.skyeye.auth.enums.UserStatus;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.repository.UserSessionRepository;
import com.skyeye.auth.service.impl.UserSessionServiceImpl;
import com.skyeye.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户会话服务测试
 */
@ExtendWith(MockitoExtension.class)
public class UserSessionServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSessionServiceImpl userSessionService;

    private User testUser;
    private UserSession testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setStatus(UserStatus.ACTIVE);

        testSession = new UserSession();
        testSession.setId(1L);
        testSession.setUserId(1L);
        testSession.setSessionToken("test-session-token");
        testSession.setRefreshToken("test-refresh-token");
        testSession.setIpAddress("192.168.1.1");
        testSession.setUserAgent("Test User Agent");
        testSession.setDeviceInfo("Test Device");
        testSession.setExpiresAt(LocalDateTime.now().plusHours(24));
        testSession.setIsActive(true);
    }

    @Test
    void testCreateSession() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(testSession);

        // When
        UserSession result = userSessionService.createSession(
                1L, "session-token", "refresh-token", 
                "192.168.1.1", "User Agent", "Device Info");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(userRepository).findById(1L);
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void testCreateSessionWithInactiveUser() {
        // Given
        testUser.setStatus(UserStatus.LOCKED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userSessionService.createSession(
                    1L, "session-token", "refresh-token", 
                    "192.168.1.1", "User Agent", "Device Info");
        });
    }

    @Test
    void testGetSessionByToken() {
        // Given
        when(userSessionRepository.findBySessionToken("test-session-token"))
                .thenReturn(Optional.of(testSession));

        // When
        UserSession result = userSessionService.getSessionByToken("test-session-token");

        // Then
        assertNotNull(result);
        assertEquals("test-session-token", result.getSessionToken());
        verify(userSessionRepository).findBySessionToken("test-session-token");
    }

    @Test
    void testGetSessionByTokenNotFound() {
        // Given
        when(userSessionRepository.findBySessionToken("invalid-token"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userSessionService.getSessionByToken("invalid-token");
        });
    }

    @Test
    void testUpdateLastAccessed() {
        // Given
        when(userSessionRepository.findBySessionToken("test-session-token"))
                .thenReturn(Optional.of(testSession));
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(testSession);

        // When
        userSessionService.updateLastAccessed("test-session-token");

        // Then
        verify(userSessionRepository).findBySessionToken("test-session-token");
        verify(userSessionRepository).save(testSession);
    }

    @Test
    void testLogoutSession() {
        // Given
        when(userSessionRepository.findBySessionToken("test-session-token"))
                .thenReturn(Optional.of(testSession));
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(testSession);

        // When
        userSessionService.logoutSession("test-session-token");

        // Then
        assertFalse(testSession.getIsActive());
        assertNotNull(testSession.getLogoutAt());
        verify(userSessionRepository).save(testSession);
    }

    @Test
    void testLogoutAllUserSessions() {
        // Given
        when(userSessionRepository.logoutAllUserSessions(eq(1L), any(LocalDateTime.class)))
                .thenReturn(3);

        // When
        userSessionService.logoutAllUserSessions(1L);

        // Then
        verify(userSessionRepository).logoutAllUserSessions(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void testGetUserActiveSessions() {
        // Given
        List<UserSession> sessions = Arrays.asList(testSession);
        when(userSessionRepository.findActiveSessionsByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(sessions);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        List<UserSessionInfo> result = userSessionService.getUserActiveSessions(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void testGetUserSessionHistory() {
        // Given
        List<UserSession> sessions = Arrays.asList(testSession);
        Page<UserSession> sessionPage = new PageImpl<>(sessions);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(userSessionRepository.findByUserId(1L, pageable)).thenReturn(sessionPage);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Page<UserSessionInfo> result = userSessionService.getUserSessionHistory(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getUserId());
    }

    @Test
    void testIsSessionValid() {
        // Given
        when(userSessionRepository.findBySessionToken("valid-token"))
                .thenReturn(Optional.of(testSession));

        // When
        boolean result = userSessionService.isSessionValid("valid-token");

        // Then
        assertTrue(result);
    }

    @Test
    void testIsSessionValidWithExpiredSession() {
        // Given
        testSession.setExpiresAt(LocalDateTime.now().minusHours(1)); // 过期会话
        when(userSessionRepository.findBySessionToken("expired-token"))
                .thenReturn(Optional.of(testSession));

        // When
        boolean result = userSessionService.isSessionValid("expired-token");

        // Then
        assertFalse(result);
    }

    @Test
    void testRefreshSession() {
        // Given
        when(userSessionRepository.findByRefreshToken("refresh-token"))
                .thenReturn(Optional.of(testSession));
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(testSession);

        // When
        UserSession result = userSessionService.refreshSession(
                "refresh-token", "new-session-token", "new-refresh-token");

        // Then
        assertNotNull(result);
        assertEquals("new-session-token", result.getSessionToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(userSessionRepository).save(testSession);
    }

    @Test
    void testCountUserActiveSessions() {
        // Given
        when(userSessionRepository.countActiveSessionsByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(2L);

        // When
        long result = userSessionService.countUserActiveSessions(1L);

        // Then
        assertEquals(2L, result);
    }

    @Test
    void testCleanupExpiredSessions() {
        // Given
        when(userSessionRepository.logoutExpiredSessions(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5);
        when(userSessionRepository.deleteExpiredSessions(any(LocalDateTime.class)))
                .thenReturn(3);

        // When
        int result = userSessionService.cleanupExpiredSessions();

        // Then
        assertEquals(8, result); // 5 + 3
        verify(userSessionRepository).logoutExpiredSessions(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(userSessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
    }
}