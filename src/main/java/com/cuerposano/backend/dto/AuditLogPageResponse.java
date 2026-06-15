package com.cuerposano.backend.dto;

import java.util.List;

public class AuditLogPageResponse {

    private List<AuditLogResponse> items;
    private int page;
    private int size;
    private long total;
    private int totalPages;

    public AuditLogPageResponse(List<AuditLogResponse> items, int page, int size, long total, int totalPages) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = totalPages;
    }

    public List<AuditLogResponse> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public int getTotalPages() { return totalPages; }
}
