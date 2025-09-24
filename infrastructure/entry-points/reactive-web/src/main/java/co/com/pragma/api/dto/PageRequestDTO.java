package co.com.pragma.api.dto;

public record PageRequestDTO(int page, int size) {
    // Un record es una forma moderna y concisa de crear clases inmutables de datos.
    // Es perfecto para nuestro dominio, ya que no tiene dependencias.
}