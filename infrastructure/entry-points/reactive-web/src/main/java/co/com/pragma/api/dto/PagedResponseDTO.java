package co.com.pragma.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDTO<T> {
    private List<T> content;
    private int currentPage;
    private long totalItems;
    private int totalPages;

    public static <T> PagedResponseDTO<T> of(List<T> content, int currentPage, long totalItems, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
        // El cálculo de totalPages se encapsula aquí.
        // Math.ceil maneja correctamente el caso de totalItems = 0.
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return new PagedResponseDTO<>(content, currentPage, totalItems, totalPages);
    }
}