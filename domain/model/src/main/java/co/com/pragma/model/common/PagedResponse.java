package co.com.pragma.model.common;


import java.util.List;

public class PagedResponse<T> {
    private List<T> content;
    private int currentPage;
    private long totalItems;
    private int totalPages;

    public PagedResponse() {
    }

    public PagedResponse(List<T> content, int currentPage, long totalItems, int totalPages) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }

    public static <T> PagedResponse<T> of(List<T> content, int currentPage, long totalItems, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
        // El cálculo de totalPages se encapsula aquí.
        // Math.ceil maneja correctamente el caso de totalItems = 0.
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return new PagedResponse<>(content, currentPage, totalItems, totalPages);
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}