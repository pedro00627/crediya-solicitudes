package co.com.pragma.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Contenedor para respuestas paginadas")
public class PagedResponseDTO<T> {
    @Schema(description = "Lista de elementos en la página actual")
    private List<T> content;
    @Schema(description = "Número de la página actual (inicia en 0)", example = "0")
    private int currentPage;
    @Schema(description = "Número total de elementos en todas las páginas", example = "100")
    private long totalItems;
    @Schema(description = "Número total de páginas", example = "10")
    private int totalPages;
}