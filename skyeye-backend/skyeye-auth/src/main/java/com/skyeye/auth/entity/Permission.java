package com.skyeye.auth.entity;

import com.skyeye.common.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 权限实体
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "name", unique = true)
})
public class Permission extends BaseEntity {

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 200, message = "权限描述长度不能超过200个字符")
    @Column(name = "description", length = 200)
    private String description;

    @Size(max = 100, message = "资源名称长度不能超过100个字符")
    @Column(name = "resource", length = 100)
    private String resource;

    @Size(max = 50, message = "操作名称长度不能超过50个字符")
    @Column(name = "action", length = 50)
    private String action;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    // Constructors
    public Permission() {}

    public Permission(String name, String description, String resource, String action) {
        this.name = name;
        this.description = description;
        this.resource = resource;
        this.action = action;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}