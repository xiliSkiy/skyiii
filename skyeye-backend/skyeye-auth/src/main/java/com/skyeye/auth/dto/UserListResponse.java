package com.skyeye.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 用户列表响应DTO
 */
@Schema(description = "用户列表响应")
public class UserListResponse {

    @Schema(description = "用户列表")
    private List<UserInfo> users;

    @Schema(description = "总数量")
    private long totalElements;

    @Schema(description = "总页数")
    private int totalPages;

    @Schema(description = "当前页码")
    private int currentPage;

    @Schema(description = "每页大小")
    private int pageSize;

    @Schema(description = "是否有下一页")
    private boolean hasNext;

    @Schema(description = "是否有上一页")
    private boolean hasPrevious;

    // Constructors
    public UserListResponse() {}

    public UserListResponse(List<UserInfo> users, long totalElements, int totalPages, 
                           int currentPage, int pageSize, boolean hasNext, boolean hasPrevious) {
        this.users = users;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    // Getters and Setters
    public List<UserInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfo> users) {
        this.users = users;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}