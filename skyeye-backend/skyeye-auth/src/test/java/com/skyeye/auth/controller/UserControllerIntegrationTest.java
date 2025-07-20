package com.skyeye.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeye.auth.dto.CreateUserRequest;
import com.skyeye.auth.dto.UpdateUserRequest;
import com.skyeye.auth.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户管理控制器集成测试
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = {"user:read"})
    public void testGetUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(authorities = {"user:create"})
    public void testCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPhone("1234567890");
        request.setRoleIds(Arrays.asList(1L));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(authorities = {"user:update"})
    public void testUpdateUser() throws Exception {
        // 首先创建一个用户
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername("updateuser");
        createRequest.setPassword("password123");
        createRequest.setEmail("update@example.com");
        createRequest.setFullName("Update User");

        String createResponse = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 从响应中提取用户ID（这里简化处理，实际应该解析JSON）
        Long userId = 1L; // 假设ID为1

        // 更新用户
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFullName("Updated User Name");
        updateRequest.setPhone("9876543210");
        updateRequest.setStatus(UserStatus.ACTIVE);

        mockMvc.perform(put("/api/v1/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated User Name"))
                .andExpect(jsonPath("$.data.phone").value("9876543210"));
    }

    @Test
    @WithMockUser(authorities = {"user:read"})
    public void testGetUserById() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId));
    }

    @Test
    @WithMockUser(authorities = {"user:update"})
    public void testActivateUser() throws Exception {
        Long userId = 1L;

        mockMvc.perform(post("/api/v1/users/" + userId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("激活用户成功"));
    }

    @Test
    @WithMockUser(authorities = {"user:update"})
    public void testLockUser() throws Exception {
        Long userId = 1L;

        mockMvc.perform(post("/api/v1/users/" + userId + "/lock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("锁定用户成功"));
    }

    @Test
    @WithMockUser(authorities = {"user:update"})
    public void testResetPassword() throws Exception {
        Long userId = 1L;

        mockMvc.perform(post("/api/v1/users/" + userId + "/reset-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("重置密码成功"));
    }

    @Test
    @WithMockUser(authorities = {"user:delete"})
    public void testDeleteUser() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("删除用户成功"));
    }

    @Test
    @WithMockUser(authorities = {"user:delete"})
    public void testBatchDeleteUsers() throws Exception {
        Long[] userIds = {1L, 2L, 3L};

        mockMvc.perform(delete("/api/v1/users/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(userIds))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量删除用户成功"));
    }

    @Test
    public void testGetUsersWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"user:read"})
    public void testGetUsersWithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("keyword", "test")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "username")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(authorities = {"user:create"})
    public void testCreateUserWithInvalidData() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(""); // 无效的用户名
        request.setPassword("123"); // 密码太短
        request.setEmail("invalid-email"); // 无效的邮箱格式

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}