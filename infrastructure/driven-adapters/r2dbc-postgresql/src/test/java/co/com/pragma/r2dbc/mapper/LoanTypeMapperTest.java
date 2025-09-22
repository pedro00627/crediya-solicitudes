package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.r2dbc.entity.LoanTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LoanTypeMapperTest {

    private LoanTypeMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new LoanTypeMapper();
    }

    @Test
    void shouldMapEntityToDomain() {
        final LoanTypeEntity entity = new LoanTypeEntity(
                1,
                "LIBRE_INVERSION",
                new BigDecimal("1000000"),
                new BigDecimal("50000000"),
                new BigDecimal("0.15"),
                true
        );

        final LoanType result = this.mapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(1, result.getLoanTypeId());
        assertEquals("LIBRE_INVERSION", result.getName());
        assertEquals(new BigDecimal("1000000"), result.getMinAmount());
        assertEquals(new BigDecimal("50000000"), result.getMaxAmount());
        assertEquals(new BigDecimal("0.15"), result.getInterestRate());
        assertTrue(result.isAutoValidation());
    }

    @Test
    void shouldMapDomainToEntity() {
        final LoanType domain = new LoanType(
                2,
                "EDUCATIVO",
                new BigDecimal("500000"),
                new BigDecimal("20000000"),
                new BigDecimal("0.12"),
                false
        );

        final LoanTypeEntity result = this.mapper.toEntity(domain);

        assertNotNull(result);
        assertEquals(2, result.getLoanTypeId());
        assertEquals("EDUCATIVO", result.getName());
        assertEquals(new BigDecimal("500000"), result.getMinAmount());
        assertEquals(new BigDecimal("20000000"), result.getMaxAmount());
        assertEquals(new BigDecimal("0.12"), result.getInterestRate());
        assertFalse(result.isAutoValidation());
    }

    @ParameterizedTest
    @MethodSource("provideLoanTypeData")
    void shouldMapBidirectionallyWithoutDataLoss(final int id, final String name, final BigDecimal minAmount,
                                                 final BigDecimal maxAmount, final BigDecimal interestRate,
                                                 final boolean autoValidation) {
        final LoanType originalDomain = new LoanType(id, name, minAmount, maxAmount, interestRate, autoValidation);

        final LoanTypeEntity entity = this.mapper.toEntity(originalDomain);
        final LoanType resultDomain = this.mapper.toDomain(entity);

        assertEquals(originalDomain.getLoanTypeId(), resultDomain.getLoanTypeId());
        assertEquals(originalDomain.getName(), resultDomain.getName());
        assertEquals(originalDomain.getMinAmount(), resultDomain.getMinAmount());
        assertEquals(originalDomain.getMaxAmount(), resultDomain.getMaxAmount());
        assertEquals(originalDomain.getInterestRate(), resultDomain.getInterestRate());
        assertEquals(originalDomain.isAutoValidation(), resultDomain.isAutoValidation());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        final LoanType result = this.mapper.toDomain(null);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        final LoanTypeEntity result = this.mapper.toEntity(null);
        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("provideEdgeCaseData")
    void shouldHandleEdgeCases(final String name, final BigDecimal minAmount, final BigDecimal maxAmount,
                               final BigDecimal interestRate, final String scenario) {
        final LoanType domain = new LoanType(1, name, minAmount, maxAmount, interestRate, true);

        final LoanTypeEntity entity = this.mapper.toEntity(domain);
        final LoanType result = this.mapper.toDomain(entity);

        assertEquals(name, result.getName(), "Failed for scenario: " + scenario);
        assertEquals(minAmount, result.getMinAmount(), "Failed for scenario: " + scenario);
        assertEquals(maxAmount, result.getMaxAmount(), "Failed for scenario: " + scenario);
        assertEquals(interestRate, result.getInterestRate(), "Failed for scenario: " + scenario);
    }

    static Stream<Arguments> provideLoanTypeData() {
        return Stream.of(
                Arguments.of(1, "LIBRE_INVERSION", new BigDecimal("1000000"), new BigDecimal("50000000"), new BigDecimal("0.15"), true),
                Arguments.of(2, "EDUCATIVO", new BigDecimal("500000"), new BigDecimal("20000000"), new BigDecimal("0.12"), false),
                Arguments.of(3, "VEHICULO", new BigDecimal("5000000"), new BigDecimal("100000000"), new BigDecimal("0.18"), true),
                Arguments.of(4, "VIVIENDA", new BigDecimal("10000000"), new BigDecimal("500000000"), new BigDecimal("0.10"), false),
                Arguments.of(0, "TEST_TYPE", BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("0.01"), true)
        );
    }

    static Stream<Arguments> provideEdgeCaseData() {
        return Stream.of(
                Arguments.of("", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Empty name and zero values"),
                Arguments.of("VERY_LONG_LOAN_TYPE_NAME_FOR_TESTING", new BigDecimal("999999999"), new BigDecimal("999999999"), new BigDecimal("0.999"), "Long name and large values"),
                Arguments.of(null, null, null, null, "All null values"),
                Arguments.of("SPECIAL_CHARS_@#$%", new BigDecimal("0.0001"), new BigDecimal("0.0002"), new BigDecimal("0.0001"), "Special characters and small decimals")
        );
    }
}