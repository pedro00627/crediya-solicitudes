package co.com.pragma.model.common;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagedResponseTest {

    @Test
    void shouldCalculateTotalPagesCorrectlyWhenItemsFitExactly() {
        // Arrange: 20 items, 10 per page -> should be exactly 2 pages
        List<String> content = Collections.nCopies(10, "item");

        // Act
        PagedResponse<String> response = PagedResponse.of(content, 0, 20, 10);

        // Assert
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getTotalItems()).isEqualTo(20);
        assertThat(response.getCurrentPage()).isZero();
    }

    @Test
    void shouldCalculateTotalPagesCorrectlyWhenThereAreRemainingItems() {
        // Arrange: 21 items, 10 per page -> should be 3 pages (2 full, 1 with remainder)
        List<String> content = Collections.nCopies(10, "item");

        // Act
        PagedResponse<String> response = PagedResponse.of(content, 0, 21, 10);

        // Assert
        assertThat(response.getTotalPages()).isEqualTo(3);
    }

    @Test
    void shouldReturnOnePageWhenTotalItemsIsLessThanPageSize() {
        // Arrange: 5 items, 10 per page -> should be 1 page
        List<String> content = Collections.nCopies(5, "item");

        // Act
        PagedResponse<String> response = PagedResponse.of(content, 0, 5, 10);

        // Assert
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

    @Test
    void shouldReturnZeroPagesWhenThereAreNoItems() {
        // Arrange: 0 items
        List<String> content = Collections.emptyList();

        // Act
        PagedResponse<String> response = PagedResponse.of(content, 0, 0, 10);

        // Assert
        assertThat(response.getTotalPages()).isZero();
    }

    @Test
    void shouldThrowExceptionForInvalidPageSize() {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> PagedResponse.of(Collections.emptyList(), 0, 10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Page size must be greater than zero.");

        assertThatThrownBy(() -> PagedResponse.of(Collections.emptyList(), 0, 10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Page size must be greater than zero.");
    }
}