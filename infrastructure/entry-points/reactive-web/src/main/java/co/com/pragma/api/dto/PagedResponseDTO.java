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

    public static <T> PagedResponseDTO<T> of(final List<T> content, final int currentPage, final long totalItems, final int pageSize) {
        if (0 >= pageSize) {
            throw new IllegalArgumentException("Page size must be greater than zero.");
        }
        // El cálculo de totalPages se encapsula aquí.
        // Math.ceil maneja correctamente el caso de totalItems = 0.
        final int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return new PagedResponseDTO<>(content, currentPage, totalItems, totalPages);
    }
}