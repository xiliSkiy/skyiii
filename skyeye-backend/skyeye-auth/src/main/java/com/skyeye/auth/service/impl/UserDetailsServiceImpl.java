package com.skyeye.auth.service.impl;

import com.skyeye.auth.entity.User;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security用户详情服务实现
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("正在加载用户: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });

        logger.debug("成功加载用户: {}, 状态: {}", username, user.getStatus());
        return UserPrincipal.create(user);
    }

    /**
     * 根据用户ID加载用户详情
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        logger.debug("正在根据ID加载用户: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("用户ID不存在: {}", userId);
                    return new UsernameNotFoundException("用户ID不存在: " + userId);
                });

        logger.debug("成功根据ID加载用户: {}, 用户名: {}", userId, user.getUsername());
        return UserPrincipal.create(user);
    }
}