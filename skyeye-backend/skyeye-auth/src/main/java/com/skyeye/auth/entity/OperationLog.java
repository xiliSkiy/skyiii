package com.skyeye.auth.entity;

import com.skyeye.common.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 操作日志实体
 */
@Entity
@Table(name = "operation_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_operation_type", columnList = "operationType"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class OperationLog extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Size(max = 100, message = "用户名长度不能超过100个字符")
    @Column(name = "username", length = 100)
    private String username;

    @NotBlank(message = "操作类型不能为空")
    @Size(max = 50, message = "操作类型长度不能超过50个字符")
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @NotBlank(message = "操作描述不能为空")
    @Size(max = 500, message = "操作描述长度不能超过500个字符")
    @Column(name = "operation_desc", nullable = false, length = 500)
    private String operationDesc;

    @Size(max = 100, message = "资源类型长度不能超过100个字符")
    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Size(max = 100, message = "资源ID长度不能超过100个字符")
    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Size(max = 20, message = "操作结果长度不能超过20个字符")
    @Column(name = "result", length = 20)
    private String result;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "execution_time")
    private Long executionTime;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    // Constructors
    public OperationLog() {}

    public OperationLog(Long userId, String username, String operationType, String operationDesc) {
        this.userId = userId;
        this.username = username;
        this.operationType = operationType;
        this.operationDesc = operationDesc;
        this.result = "SUCCESS";
    }

    // Helper methods
    public void markAsSuccess() {
        this.result = "SUCCESS";
    }

    public void markAsFailure(String errorMessage) {
        this.result = "FAILURE";
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(result);
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
}