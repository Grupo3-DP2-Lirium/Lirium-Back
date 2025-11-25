package org.example.springboot_backend.dto;

import java.util.List;

public class MemoryPageResponse {

    private int page;
    private int size;
    private int totalPages;
    private long totalItems;
    private List<MemoryResponse> items;

    public MemoryPageResponse() {}

    public MemoryPageResponse(int page, int size, int totalPages, long totalItems, List<MemoryResponse> items) {
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public List<MemoryResponse> getItems() {
        return items;
    }

    public void setItems(List<MemoryResponse> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "MemoryPageResponse{" +
                "page=" + page +
                ", size=" + size +
                ", totalPages=" + totalPages +
                ", totalItems=" + totalItems +
                ", items=" + items +
                '}';
    }
}
