package com.skyeye.auth.service.impl;

import com.skyeye.auth.dto.*;
import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.enums.UserRole;
import com.skyeye.auth.enums.UserStatus;
import com.skyeye.auth.repository.RoleRepository;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.security.UserPrincipal;
import com.skyeye.auth.service.AuthService;
import com.skyeye.auth.util.JwtUtil;
import com.skyeye.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("用户登录尝试: {}", loginRequest.getUsername());

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // 更新最后登录时间和重置登录失败次数
            User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
            
            user.setLastLoginAt(LocalDateTime.now());
            user.setLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);

            // 生成JWT令牌
            String accessToken = jwtUtil.generateTokenWithUserId(userPrincipal, userPrincipal.getId());
            String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);

            // 构建用户信息
            UserInfo userInfo = buildUserInfo(user);

            logger.info("用户 {} 登录成功", loginRequest.getUsername());
            return new LoginResponse(accessToken, refreshToken, jwtExpiration / 1000, userInfo);

        } catch (BadCredentialsException e) {
            handleLoginFailure(loginRequest.getUsername());
            throw new BusinessException("用户名或密码错误");
        } catch (LockedException e) {
            throw new BusinessException("账户已被锁定");
        } catch (DisabledException e) {
            throw new BusinessException("账户已被禁用");
        } catch (Exception e) {
            logger.error("登录过程中发生错误", e);
            throw new BusinessException("登录失败，请稍后重试");
        }
    }

    @Override
    public UserInfo register(RegisterRequest registerRequest) {
        logger.info("用户注册尝试: {}", registerRequest.getUsername());

        // 验证密码确认
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException("密码和确认密码不匹配");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setPhone(registerRequest.getPhone());
        user.setStatus(UserStatus.ACTIVE);

        // 分配默认角色（查看者）
        Role viewerRole = roleRepository.findByName(UserRole.VIEWER)
            .orElseThrow(() -> new BusinessException("默认角色不存在，请联系管理员"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(viewerRole);
        user.setRoles(roles);

        user = userRepository.save(user);

        logger.info("用户 {} 注册成功", registerRequest.getUsername());
        return buildUserInfo(user);
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        try {
            // 验证刷新令牌
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new BusinessException("无效的刷新令牌");
            }

            String username = jwtUtil.getUsernameFromToken(refreshToken);
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

            UserPrincipal userPrincipal = UserPrincipal.create(user);

            if (jwtUtil.validateToken(refreshToken, userPrincipal)) {
                // 生成新的访问令牌
                String newAccessToken = jwtUtil.generateTokenWithUserId(userPrincipal, user.getId());
                String newRefreshToken = jwtUtil.generateRefreshToken(userPrincipal);

                UserInfo userInfo = buildUserInfo(user);

                logger.info("用户 {} 刷新令牌成功", username);
                return new LoginResponse(newAccessToken, newRefreshToken, jwtExpiration / 1000, userInfo);
            } else {
                throw new BusinessException("刷新令牌已过期");
            }
        } catch (Exception e) {
            logger.error("刷新令牌失败", e);
            throw new BusinessException("刷新令牌失败");
        }
    }

    @Override
    public void logout(String token) {
        // 这里可以实现令牌黑名单机制
        // 目前简单地清除SecurityContext
        SecurityContextHolder.clearContext();
        logger.info("用户注销成功");
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new BusinessException("用户不存在"));

        return buildUserInfo(user);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateTokenFormat(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getLoginAttempts() + 1;
            user.setLoginAttempts(attempts);

            // 如果失败次数达到5次，锁定账户30分钟
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                logger.warn("用户 {} 登录失败次数过多，账户已被锁定", username);
            }

            userRepository.save(user);
        });
    }

    /**
     * 构建用户信息DTO
     */
    private UserInfo buildUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setFullName(user.getFullName());
        userInfo.setPhone(user.getPhone());
        userInfo.setStatus(user.getStatus());
        userInfo.setLastLoginAt(user.getLastLoginAt());

        // 设置角色
        Set<UserRole> roles = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
        userInfo.setRoles(roles);

        // 设置权限
        Set<String> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());
        userInfo.setPermissions(permissions);

        return userInfo;
    }
}