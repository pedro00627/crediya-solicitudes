package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.ApplicationReviewDTO;
import co.com.pragma.api.dto.request.ApplicationRequestRecord;
import co.com.pragma.api.dto.response.ApplicationResponseRecord;
import co.com.pragma.api.dto.response.LoanTypeResponseRecord;
import co.com.pragma.api.dto.response.ResponseRecord;
import co.com.pragma.api.dto.response.StatusResponseRecord;
import co.com.pragma.model.application.Application;
import co.com.pragma.model.application.ApplicationCreationResult;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.user.UserRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IApplicationMapperTest {

    private IApplicationMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new IApplicationMapperImpl();
    }

    @Test
    void shouldMapApplicationCreationResultToResponse() {
        final Application application = this.createTestApplication();
        final LoanType loanType = this.createTestLoanType();
        final Status status = this.createTestStatus();
        final UserRecord user = new UserRecord(
                "1",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "user@test.com",
                "12345678",
                "555-1234",
                1,
                5000000.0
        );

        final ApplicationCreationResult result = new ApplicationCreationResult(application, loanType, status, user);

        final ResponseRecord response = this.mapper.toResponse(result);

        assertNotNull(response);
        assertNotNull(response.applicationResponseRecord());
        assertNotNull(response.statusResponseRecord());
        assertNotNull(response.loanTypeResponseRecord());

        assertEquals(application.getApplicationId(), response.applicationResponseRecord().applicationId());
        assertEquals(application.getAmount(), response.applicationResponseRecord().amount());
        assertEquals(status.getName(), response.statusResponseRecord().name());
        assertEquals(loanType.getName(), response.loanTypeResponseRecord().name());
    }

    @Test
    void shouldMapApplicationRequestToApplication() {
        final ApplicationRequestRecord request = new ApplicationRequestRecord(
                UUID.randomUUID(),
                new BigDecimal("5000000"),
                24,
                "test@example.com",
                "PENDING_REVIEW",
                "LIBRE_INVERSION"
        );

        final Application result = this.mapper.toApplication(request);

        assertNotNull(result);
        assertEquals(request.amount(), result.getAmount());
        assertEquals(request.term(), result.getTerm());
        assertEquals(request.email(), result.getEmail());
        assertNull(result.getApplicationId());
        assertEquals(0, result.getStatusId());
        assertEquals(0, result.getLoanTypeId());
    }

    @Test
    void shouldMapToReviewDTO() {
        final Application application = this.createTestApplication();
        final LoanType loanType = this.createTestLoanType();
        final String statusName = "PENDING_REVIEW";

        final ApplicationReviewDTO result = this.mapper.toReviewDTO(application, statusName, loanType);

        assertNotNull(result);
        assertEquals(application.getAmount(), result.getAmount());
        assertEquals(application.getTerm(), result.getTerm());
        assertEquals(application.getEmail(), result.getEmail());
        assertEquals(loanType.getName(), result.getLoanType());
        assertEquals(loanType.getInterestRate().doubleValue(), result.getInterestRate());
        assertEquals(statusName, result.getApplicationStatus());
    }

    @Test
    void shouldMapApplicationToApplicationResponseRecord() {
        final Application application = this.createTestApplication();

        final ApplicationResponseRecord result = this.mapper.toApplicationResponseRecord(application);

        assertNotNull(result);
        assertEquals(application.getApplicationId(), result.applicationId());
        assertEquals(application.getAmount(), result.amount());
        assertEquals(application.getTerm(), result.term());
        assertEquals(application.getEmail(), result.email());
        assertEquals(application.getStatusId(), result.statusId());
        assertEquals(application.getLoanTypeId(), result.loanTypeId());
    }

    @Test
    void shouldMapStatusToStatusResponseRecord() {
        final Status status = this.createTestStatus();

        final StatusResponseRecord result = this.mapper.toStatusResponseRecord(status);

        assertNotNull(result);
        assertEquals(status.getStatusId(), result.statusId());
        assertEquals(status.getName(), result.name());
        assertEquals(status.getDescription(), result.description());
    }

    @Test
    void shouldMapLoanTypeToLoanTypeResponseRecord() {
        final LoanType loanType = this.createTestLoanType();

        final LoanTypeResponseRecord result = this.mapper.toLoanTypeResponseRecord(loanType);

        assertNotNull(result);
        assertEquals(loanType.getLoanTypeId(), result.loanTypeId());
        assertEquals(loanType.getName(), result.name());
        assertEquals(loanType.getMinAmount(), result.minAmount());
        assertEquals(loanType.getMaxAmount(), result.maxAmount());
        assertEquals(loanType.getInterestRate(), result.interestRate());
        assertEquals(loanType.isAutoValidation(), result.autoValidation());
    }

    @ParameterizedTest
    @MethodSource("provideNullInputs")
    void shouldHandleNullInputs(final Object input, final String scenario) {
        switch (scenario) {
            case "ApplicationCreationResult" -> {
                final ResponseRecord result = this.mapper.toResponse((ApplicationCreationResult) input);
                assertNull(result);
            }
            case "ApplicationRequestRecord" -> {
                final Application result = this.mapper.toApplication((ApplicationRequestRecord) input);
                assertNull(result);
            }
            case "Application" -> {
                final ApplicationResponseRecord result = this.mapper.toApplicationResponseRecord((Application) input);
                assertNull(result);
            }
            case "Status" -> {
                final StatusResponseRecord result = this.mapper.toStatusResponseRecord((Status) input);
                assertNull(result);
            }
            case "LoanType" -> {
                final LoanTypeResponseRecord result = this.mapper.toLoanTypeResponseRecord((LoanType) input);
                assertNull(result);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideReviewDTOInputs")
    void shouldHandleReviewDTONullInputs(final Application application, final String statusName, final LoanType loanType, final boolean shouldBeNull) {
        final ApplicationReviewDTO result = this.mapper.toReviewDTO(application, statusName, loanType);

        if (shouldBeNull) {
            assertNull(result);
        } else {
            assertNotNull(result);
        }
    }

    @ParameterizedTest
    @MethodSource("provideApplicationRequestVariations")
    void shouldMapVariousApplicationRequests(final BigDecimal amount, final int term, final String email, final String scenario) {
        final ApplicationRequestRecord request = new ApplicationRequestRecord(
                UUID.randomUUID(),
                amount,
                term,
                email,
                "PENDING_REVIEW",
                "LIBRE_INVERSION"
        );

        final Application result = this.mapper.toApplication(request);

        assertNotNull(result, "Failed for scenario: " + scenario);
        assertEquals(amount, result.getAmount(), "Failed for scenario: " + scenario);
        assertEquals(term, result.getTerm(), "Failed for scenario: " + scenario);
        assertEquals(email, result.getEmail(), "Failed for scenario: " + scenario);
    }

    @ParameterizedTest
    @MethodSource("provideLoanTypeVariations")
    void shouldMapVariousLoanTypes(final int id, final String name, final BigDecimal minAmount, final BigDecimal maxAmount,
                                   final BigDecimal interestRate, final boolean autoValidation, final String scenario) {
        final LoanType loanType = new LoanType(id, name, minAmount, maxAmount, interestRate, autoValidation);

        final LoanTypeResponseRecord result = this.mapper.toLoanTypeResponseRecord(loanType);

        assertNotNull(result, "Failed for scenario: " + scenario);
        assertEquals(id, result.loanTypeId(), "Failed for scenario: " + scenario);
        assertEquals(name, result.name(), "Failed for scenario: " + scenario);
        assertEquals(interestRate, result.interestRate(), "Failed for scenario: " + scenario);
        assertEquals(autoValidation, result.autoValidation(), "Failed for scenario: " + scenario);
    }

    static Stream<Arguments> provideNullInputs() {
        return Stream.of(
                Arguments.of(null, "ApplicationCreationResult"),
                Arguments.of(null, "ApplicationRequestRecord"),
                Arguments.of(null, "Application"),
                Arguments.of(null, "Status"),
                Arguments.of(null, "LoanType")
        );
    }

    static Stream<Arguments> provideReviewDTOInputs() {
        final Application app = new Application(UUID.randomUUID(), "12345", new BigDecimal("1000000"), 12, "test@test.com", 1, 1, null, null ,null ,null ,null, null);
        final LoanType loan = new LoanType(1, "TEST", new BigDecimal("1000"), new BigDecimal("10000"), new BigDecimal("0.15"), true);

        return Stream.of(
                Arguments.of(null, null, null, true),  // All null should return null
                Arguments.of(app, "PENDING", loan, false), // Valid inputs
                Arguments.of(app, null, null, false), // Only application
                Arguments.of(null, "PENDING", loan, false), // No application
                Arguments.of(null, null, loan, false) // Only loan type
        );
    }

    static Stream<Arguments> provideApplicationRequestVariations() {
        return Stream.of(
                Arguments.of(new BigDecimal("1000000"), 12, "test@example.com", "Standard application"),
                Arguments.of(new BigDecimal("50000000"), 60, "user@domain.co", "Large amount, long term"),
                Arguments.of(new BigDecimal("500000"), 6, "client@bank.com", "Small amount, short term"),
                Arguments.of(BigDecimal.ZERO, 1, "", "Edge case values"),
                Arguments.of(new BigDecimal("999999999"), 120, "very.long.email.address@very.long.domain.extension.com", "Maximum values")
        );
    }

    static Stream<Arguments> provideLoanTypeVariations() {
        return Stream.of(
                Arguments.of(1, "LIBRE_INVERSION", new BigDecimal("1000000"), new BigDecimal("50000000"), new BigDecimal("0.15"), true, "Standard free investment loan"),
                Arguments.of(2, "EDUCATIVO", new BigDecimal("500000"), new BigDecimal("20000000"), new BigDecimal("0.12"), false, "Educational loan"),
                Arguments.of(3, "VEHICULO", new BigDecimal("5000000"), new BigDecimal("100000000"), new BigDecimal("0.18"), true, "Vehicle loan"),
                Arguments.of(0, "", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, "Edge case loan type"),
                Arguments.of(999, "SPECIAL_LOAN_TYPE_WITH_LONG_NAME", new BigDecimal("0.01"), new BigDecimal("999999999"), new BigDecimal("0.99"), true, "Extreme values loan type")
        );
    }

    private Application createTestApplication() {
        return new Application(
                UUID.randomUUID(),
                "12345678",
                new BigDecimal("5000000"),
                24,
                "test@example.com",
                1,
                1,
                null, null,null,null,null,null
                );
    }

    private LoanType createTestLoanType() {
        return new LoanType(
                1,
                "LIBRE_INVERSION",
                new BigDecimal("1000000"),
                new BigDecimal("50000000"),
                new BigDecimal("0.15"),
                true
        );
    }

    private Status createTestStatus() {
        return new Status(1, "PENDING_REVIEW", "Solicitud pendiente de revisión");
    }
}