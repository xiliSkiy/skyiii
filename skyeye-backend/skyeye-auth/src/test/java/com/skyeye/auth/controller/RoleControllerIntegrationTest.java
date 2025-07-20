package com.skyeye.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeye.auth.dto.AssignRoleRequest;
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
 * 角色管理控制器集成测试
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = {"role:read"})
    public void testGetAllRoles() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(authorities = {"role:read"})
    public void testGetRoleById() throws Exception {
        Long roleId = 1L;

        mockMvc.perform(get("/api/v1/roles/" + roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(roleId));
    }

    @Test
    @WithMockUser(authorities = {"role:manage"})
    public void testAssignRolesToUser() throws Exception {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setUserId(1L);
        request.setRoleIds(Arrays.asList(1L, 2L));

        mockMvc.perform(post("/api/v1/roles/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("分配角色成功"));
    }

    @Test
    @WithMockUser(authorities = {"role:manage"})
    public void testRemoveRolesFromUser() throws Exception {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setUserId(1L);
        request.setRoleIds(Arrays.asList(2L));

        mockMvc.perform(delete("/api/v1/roles/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("移除角色成功"));
    }

    @Test
    @WithMockUser(authorities = {"role:read"})
    public void testGetUserRoles() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/v1/roles/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetRolesWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"role:manage"})
    public void testAssignRolesWithInvalidData() throws Exception {
        AssignRoleRequest request = new AssignRoleRequest();
        // 缺少必要的字段

        mockMvc.perform(post("/api/v1/roles/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}