package com.skyeye.auth.security;

import com.skyeye.auth.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限验证拦截器
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("权限拦截器检查请求: {} {}", method, requestURI);

        // 跳过公开接口
        if (isPublicEndpoint(requestURI)) {
            logger.debug("公开接口，跳过权限检查: {}", requestURI);
            return true;
        }

        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("用户未认证，跳过权限检查");
            return true; // 让Spring Security处理认证
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        // 根据请求路径和方法确定所需权限
        String requiredPermission = determineRequiredPermission(requestURI, method);
        if (requiredPermission == null) {
            logger.debug("无需特定权限，允许访问: {}", requestURI);
            return true;
        }

        // 检查用户权限
        boolean hasPermission = permissionService.hasPermission(userId, requiredPermission);
        if (!hasPermission) {
            logger.warn("用户 {} 缺少权限 {} 访问 {}", userId, requiredPermission, requestURI);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":403,\"message\":\"权限不足\"}");
            return false;
        }

        logger.debug("用户 {} 拥有权限 {}，允许访问 {}", userId, requiredPermission, requestURI);
        return true;
    }

    /**
     * 判断是否为公开接口
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/v1/auth/login") ||
               requestURI.startsWith("/api/v1/auth/register") ||
               requestURI.startsWith("/api/v1/auth/refresh") ||
               requestURI.startsWith("/actuator/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.startsWith("/v3/api-docs/");
    }

    /**
     * 根据请求路径和方法确定所需权限
     */
    private String determineRequiredPermission(String requestURI, String method) {
        // 用户管理相关权限
        if (requestURI.startsWith("/api/v1/users")) {
            switch (method) {
                case "GET":
                    return "user:read";
                case "POST":
                    return "user:create";
                case "PUT":
                case "PATCH":
                    return "user:update";
                case "DELETE":
                    return "user:delete";
            }
        }

        // 设备管理相关权限
        if (requestURI.startsWith("/api/v1/devices")) {
            switch (method) {
                case "GET":
                    return "device:read";
                case "POST":
                    return "device:create";
                case "PUT":
                case "PATCH":
                    return "device:update";
                case "DELETE":
                    return "device:delete";
            }
        }

        // 监控相关权限
        if (requestURI.startsWith("/api/v1/monitor")) {
            return "monitor:view";
        }

        // 报警相关权限
        if (requestURI.startsWith("/api/v1/alerts")) {
            switch (method) {
                case "GET":
                    return "alert:read";
                case "PUT":
                case "PATCH":
                    return "alert:handle";
                case "POST":
                    return "alert:config";
            }
        }

        // 系统配置相关权限
        if (requestURI.startsWith("/api/v1/system")) {
            return "system:config";
        }

        // 日志相关权限
        if (requestURI.startsWith("/api/v1/logs")) {
            return "log:read";
        }

        // 默认无需特定权限
        return null;
    }
}