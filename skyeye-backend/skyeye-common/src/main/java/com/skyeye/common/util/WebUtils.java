package com.skyeye.common.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Web工具类
 */
public class WebUtils {

    private static final String[] IP_HEADER_NAMES = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };

    /**
     * 获取客户端真实IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = null;
        
        // 尝试从各种代理头中获取真实IP
        for (String headerName : IP_HEADER_NAMES) {
            ip = request.getHeader(headerName);
            if (isValidIp(ip)) {
                break;
            }
        }

        // 如果代理头中没有找到有效IP，则使用远程地址
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }

    /**
     * 检查IP是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && 
               !ip.isEmpty() && 
               !"unknown".equalsIgnoreCase(ip) &&
               !"0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * 获取用户代理字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }

    /**
     * 获取请求URI
     */
    public static String getRequestUri(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getRequestURI();
    }

    /**
     * 获取请求方法
     */
    public static String getRequestMethod(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getMethod();
    }

    /**
     * 判断是否为Ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    /**
     * 获取完整的请求URL
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        
        if (queryString != null && !queryString.isEmpty()) {
            url.append("?").append(queryString);
        }
        
        return url.toString();
    }
}