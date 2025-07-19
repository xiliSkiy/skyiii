package com.skyeye.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API网关启动类
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 认证服务路由
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri("http://localhost:8081"))
                // 设备管理服务路由
                .route("device-service", r -> r.path("/api/v1/devices/**")
                        .uri("http://localhost:8082"))
                // 视频服务路由
                .route("video-service", r -> r.path("/api/v1/video/**")
                        .uri("http://localhost:8083"))
                // AI分析服务路由
                .route("ai-service", r -> r.path("/api/v1/ai/**")
                        .uri("http://localhost:8084"))
                // 报警服务路由
                .route("alert-service", r -> r.path("/api/v1/alerts/**")
                        .uri("http://localhost:8085"))
                // 数据分析服务路由
                .route("analytics-service", r -> r.path("/api/v1/analytics/**")
                        .uri("http://localhost:8086"))
                // 任务调度服务路由
                .route("task-service", r -> r.path("/api/v1/tasks/**")
                        .uri("http://localhost:8087"))
                .build();
    }
}