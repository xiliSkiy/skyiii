package com.skyeye.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页响应格式
 */
@Schema(description = "分页响应格式")
public class PageResponse<T> {
    
    @Schema(description = "数据列表")
    private List<T> items;
    
    @Schema(description = "总记录数", example = "100")
    private long total;
    
    @Schema(description = "当前页码", example = "1")
    private int page;
    
    @Schema(description = "每页大小", example = "10")
    private int pageSize;
    
    @Schema(description = "是否有下一页", example = "true")
    private boolean hasNext;

    public PageResponse() {}

    public PageResponse(List<T> items, long total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.hasNext = (long) page * pageSize < total;
    }

    // Getters and Setters
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
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
}