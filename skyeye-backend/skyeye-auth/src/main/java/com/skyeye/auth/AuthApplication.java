package com.skyeye.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 认证服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.skyeye.auth", "com.skyeye.common"})
@EntityScan(basePackages = {"com.skyeye.auth.entity", "com.skyeye.common.entity"})
@EnableJpaAuditing
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}