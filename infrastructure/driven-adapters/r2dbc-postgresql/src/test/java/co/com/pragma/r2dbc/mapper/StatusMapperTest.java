package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.status.Status;
import co.com.pragma.r2dbc.entity.StatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StatusMapperTest {

    private StatusMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new StatusMapper();
    }

    @Test
    void shouldMapEntityToDomain() {
        final StatusEntity entity = new StatusEntity(
                1,
                "PENDING_REVIEW",
                "Solicitud pendiente de revisión"
        );

        final Status result = this.mapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(1, result.getStatusId());
        assertEquals("PENDING_REVIEW", result.getName());
        assertEquals("Solicitud pendiente de revisión", result.getDescription());
    }

    @Test
    void shouldMapDomainToEntity() {
        final Status domain = new Status(
                2,
                "APPROVED",
                "Solicitud aprobada"
        );

        final StatusEntity result = this.mapper.toEntity(domain);

        assertNotNull(result);
        assertEquals(2, result.getStatusId());
        assertEquals("APPROVED", result.getName());
        assertEquals("Solicitud aprobada", result.getDescription());
    }

    @ParameterizedTest
    @MethodSource("provideStatusData")
    void shouldMapBidirectionallyWithoutDataLoss(final int id, final String name, final String description) {
        final Status originalDomain = new Status(id, name, description);

        final StatusEntity entity = this.mapper.toEntity(originalDomain);
        final Status resultDomain = this.mapper.toDomain(entity);

        assertEquals(originalDomain.getStatusId(), resultDomain.getStatusId());
        assertEquals(originalDomain.getName(), resultDomain.getName());
        assertEquals(originalDomain.getDescription(), resultDomain.getDescription());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        final Status result = this.mapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        final StatusEntity result = this.mapper.toEntity(null);
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideEdgeCaseData")
    void shouldHandleEdgeCases(final String name, final String description, final String scenario) {
        final Status domain = new Status(1, name, description);

        final StatusEntity entity = this.mapper.toEntity(domain);
        final Status result = this.mapper.toDomain(entity);

        assertEquals(name, result.getName(), "Failed for scenario: " + scenario);
        assertEquals(description, result.getDescription(), "Failed for scenario: " + scenario);
    }

    @ParameterizedTest
    @MethodSource("provideStatusBusinessRules")
    void shouldMapCommonBusinessStatuses(final int id, final String statusName, final String expectedDescription, final String businessRule) {
        final Status domain = new Status(id, statusName, expectedDescription);

        final StatusEntity entity = this.mapper.toEntity(domain);
        final Status result = this.mapper.toDomain(entity);

        assertEquals(statusName, result.getName(), "Business rule: " + businessRule);
        assertEquals(expectedDescription, result.getDescription(), "Business rule: " + businessRule);
        assertEquals(id, result.getStatusId(), "Business rule: " + businessRule);
    }

    static Stream<Arguments> provideStatusData() {
        return Stream.of(
                Arguments.of(1, "PENDING_REVIEW", "Solicitud pendiente de revisión"),
                Arguments.of(2, "APPROVED", "Solicitud aprobada"),
                Arguments.of(3, "REJECTED", "Solicitud rechazada"),
                Arguments.of(4, "MANUAL_REVIEW", "Solicitud requiere revisión manual"),
                Arguments.of(5, "CANCELLED", "Solicitud cancelada por el cliente"),
                Arguments.of(0, "DRAFT", "Borrador de solicitud")
        );
    }

    static Stream<Arguments> provideEdgeCaseData() {
        return Stream.of(
                Arguments.of("", "", "Empty name and description"),
                Arguments.of("VERY_LONG_STATUS_NAME_FOR_TESTING_PURPOSES", "Very long description for testing purposes that might exceed normal limits", "Long strings"),
                Arguments.of(null, null, "Null values"),
                Arguments.of("STATUS_WITH_SPECIAL_CHARS_@#$%", "Description with special characters: áéíóú ñ", "Special characters"),
                Arguments.of("UNICODE_STATUS_TEST", "Unicode description with accents: áéíóú", "Unicode characters")
        );
    }

    static Stream<Arguments> provideStatusBusinessRules() {
        return Stream.of(
                Arguments.of(1, "PENDING_REVIEW", "Solicitud pendiente de revisión", "Initial status for new applications"),
                Arguments.of(2, "APPROVED", "Solicitud aprobada", "Application meets all criteria"),
                Arguments.of(3, "REJECTED", "Solicitud rechazada", "Application does not meet criteria"),
                Arguments.of(4, "MANUAL_REVIEW", "Solicitud requiere revisión manual", "Application needs advisor review"),
                Arguments.of(5, "CANCELLED", "Solicitud cancelada", "Client cancelled application"),
                Arguments.of(6, "DISBURSED", "Préstamo desembolsado", "Loan amount has been disbursed"),
                Arguments.of(7, "DEFAULTED", "En mora", "Payment is overdue")
        );
    }
}