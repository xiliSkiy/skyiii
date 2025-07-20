package com.skyeye.auth.dto;

import com.skyeye.auth.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 更新用户请求DTO
 */
@Schema(description = "更新用户请求")
public class UpdateUserRequest {

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "姓名")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String fullName;

    @Schema(description = "电话号码")
    @Size(max = 20, message = "电话号码长度不能超过20个字符")
    private String phone;

    @Schema(description = "用户状态")
    private UserStatus status;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    // Constructors
    public UpdateUserRequest() {}

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}