package co.com.pragma.model.common;


import java.util.List;

public class PagedResponse<T> {
    private List<T> content;
    private int currentPage;
    private long totalItems;
    private int totalPages;

    public PagedResponse() {
    }

    public PagedResponse(final List<T> content, final int currentPage, final long totalItems, final int totalPages) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }

    public static <T> PagedResponse<T> of(final List<T> content, final int currentPage, final long totalItems, final int pageSize) {
        if (0 >= pageSize) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
        // El cálculo de totalPages se encapsula aquí.
        // Math.ceil maneja correctamente el caso de totalItems = 0.
        final int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return new PagedResponse<>(content, currentPage, totalItems, totalPages);
    }

    public List<T> getContent() {
        return this.content;
    }

    public void setContent(final List<T> content) {
        this.content = content;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(final int currentPage) {
        this.currentPage = currentPage;
    }

    public long getTotalItems() {
        return this.totalItems;
    }

    public void setTotalItems(final long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public void setTotalPages(final int totalPages) {
        this.totalPages = totalPages;
    }
}