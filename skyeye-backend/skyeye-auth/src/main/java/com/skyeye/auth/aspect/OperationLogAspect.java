package com.skyeye.auth.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeye.auth.service.OperationLogService;
import com.skyeye.common.util.SecurityUtils;
import com.skyeye.common.util.WebUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点：所有Controller的公共方法
     */
    @Pointcut("execution(public * com.skyeye.auth.controller..*(..))")
    public void controllerMethods() {}

    /**
     * 环绕通知：记录操作日志
     */
    @Around("controllerMethods()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }
        
        String operationType = getOperationType(joinPoint);
        String operationDesc = getOperationDesc(joinPoint);
        String resourceType = getResourceType(joinPoint);
        String resourceId = getResourceId(joinPoint);
        
        Long userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        String ipAddress = WebUtils.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        Object result = null;
        String requestData = null;
        String responseData = null;
        
        try {
            // 记录请求参数
            requestData = getRequestData(joinPoint);
            
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 记录响应数据
            responseData = getResponseData(result);
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 异步记录成功操作日志
            operationLogService.logOperationAsync(userId, username, operationType, operationDesc,
                    resourceType, resourceId, ipAddress, userAgent);
            
            return result;
            
        } catch (Exception e) {
            // 记录失败操作日志
            operationLogService.logFailedOperation(userId, username, operationType, operationDesc,
                    resourceType, resourceId, ipAddress, userAgent, e.getMessage());
            throw e;
        }
    }

    /**
     * 异常通知：记录异常操作日志
     */
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return;
        }
        
        String operationType = getOperationType(joinPoint);
        String operationDesc = getOperationDesc(joinPoint);
        String resourceType = getResourceType(joinPoint);
        String resourceId = getResourceId(joinPoint);
        
        Long userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        String ipAddress = WebUtils.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        operationLogService.logFailedOperation(userId, username, operationType, operationDesc,
                resourceType, resourceId, ipAddress, userAgent, ex.getMessage());
    }

    /**
     * 获取当前HTTP请求
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取操作类型
     */
    private String getOperationType(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        // 根据方法名推断操作类型
        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("modify")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("list") || methodName.startsWith("query")) {
            return "READ";
        } else if (methodName.contains("login")) {
            return "LOGIN";
        } else if (methodName.contains("logout")) {
            return "LOGOUT";
        } else {
            return "OTHER";
        }
    }

    /**
     * 获取操作描述
     */
    private String getOperationDesc(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }

    /**
     * 获取资源类型
     */
    private String getResourceType(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        if (className.contains("User")) {
            return "USER";
        } else if (className.contains("Role")) {
            return "ROLE";
        } else if (className.contains("Permission")) {
            return "PERMISSION";
        } else if (className.contains("Session")) {
            return "SESSION";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 获取资源ID
     */
    private String getResourceId(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        
        // 尝试从参数中获取ID
        for (Object arg : args) {
            if (arg instanceof Long) {
                return arg.toString();
            } else if (arg instanceof String && arg.toString().matches("\\d+")) {
                return arg.toString();
            }
        }
        
        return null;
    }

    /**
     * 获取请求数据
     */
    private String getRequestData(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 过滤掉HttpServletRequest等不需要序列化的参数
                Object[] filteredArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof HttpServletRequest || 
                        args[i] instanceof javax.servlet.http.HttpServletResponse) {
                        filteredArgs[i] = null;
                    } else {
                        filteredArgs[i] = args[i];
                    }
                }
                return objectMapper.writeValueAsString(filteredArgs);
            }
        } catch (Exception e) {
            logger.warn("序列化请求参数失败", e);
        }
        return null;
    }

    /**
     * 获取响应数据
     */
    private String getResponseData(Object result) {
        try {
            if (result != null) {
                return objectMapper.writeValueAsString(result);
            }
        } catch (Exception e) {
            logger.warn("序列化响应数据失败", e);
        }
        return null;
    }
}