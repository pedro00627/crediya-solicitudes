package co.com.pragma.usecase.validator;

import co.com.pragma.model.application.Application;
import co.com.pragma.model.constants.ValidationMessages;
import co.com.pragma.model.exception.BusinessException;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.user.UserRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive business-focused tests for ApplicationValidator.
 * Tests validate business rules from user stories HU1-HU11.
 * Uses parameterized testing to cover all business scenarios.
 */
@DisplayName("ApplicationValidator - Business Rule Validation")
class ApplicationValidatorTest {

    // ============ USER ROLE VALIDATION TESTS (HU2, HU4, HU5) ============

    @ParameterizedTest
    @MethodSource("validUserRoleScenarios")
    @DisplayName("Should validate user roles successfully for business scenarios")
    void shouldValidateUserRolesSuccessfully(final UserRecord user, final Integer requiredRoleId, final String businessScenario) {
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> ApplicationValidator.validateUserRole(user, requiredRoleId),
                "Valid user role should not throw exception for: " + businessScenario);
    }

    @ParameterizedTest
    @MethodSource("invalidUserRoleScenarios")
    @DisplayName("Should reject invalid user roles with business exception")
    void shouldRejectInvalidUserRoles(final UserRecord user, final Integer requiredRoleId, final String businessScenario) {
        // When & Then
        final BusinessException exception = assertThrows(BusinessException.class,
                () -> ApplicationValidator.validateUserRole(user, requiredRoleId),
                "Invalid user role should throw BusinessException for: " + businessScenario);

        assertEquals(ValidationMessages.INVALID_USER_ROLE, exception.getMessage(),
                "Should use centralized error message for: " + businessScenario);
    }

    @Test
    @DisplayName("Should handle null user scenarios")
    void shouldHandleNullUserScenarios() {
        // Given
        final UserRecord nullUser = null;
        final Integer anyRoleId = ValidationMessages.CLIENT_ROLE_ID;

        // When & Then
        final BusinessException exception = assertThrows(BusinessException.class,
                () -> ApplicationValidator.validateUserRole(nullUser, anyRoleId),
                "Null user should throw BusinessException");

        assertEquals(ValidationMessages.INVALID_USER_ROLE, exception.getMessage());
    }

    // ============ LOAN AMOUNT VALIDATION TESTS (HU3) ============

    @ParameterizedTest
    @MethodSource("validLoanAmountScenarios")
    @DisplayName("Should validate loan amounts within business limits")
    void shouldValidateLoanAmountsWithinLimits(final Application application, final LoanType loanType, final String businessScenario) {
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> ApplicationValidator.validateLoanAmount(application, loanType),
                "Valid loan amount should not throw exception for: " + businessScenario);
    }

    @ParameterizedTest
    @MethodSource("invalidLoanAmountScenarios")
    @DisplayName("Should reject loan amounts outside business limits")
    void shouldRejectInvalidLoanAmounts(final Application application, final LoanType loanType, final String businessScenario) {
        // When & Then
        final BusinessException exception = assertThrows(BusinessException.class,
                () -> ApplicationValidator.validateLoanAmount(application, loanType),
                "Invalid loan amount should throw BusinessException for: " + businessScenario);

        assertEquals(ValidationMessages.LOAN_AMOUNT_OUT_OF_RANGE, exception.getMessage(),
                "Should use centralized error message for: " + businessScenario);
    }

    @ParameterizedTest
    @CsvSource({
            "1000000, 500000, 2000000, 'Libre Inversión - Minimum boundary'",
            "2000000, 500000, 2000000, 'Libre Inversión - Maximum boundary'",
            "5000000, 1000000, 10000000, 'Educativo - Middle range'",
            "10000000, 1000000, 10000000, 'Educativo - Maximum boundary'"
    })
    @DisplayName("Should validate boundary conditions for loan amounts")
    void shouldValidateBoundaryConditions(final String amountStr, final String minAmountStr, final String maxAmountStr, final String scenario) {
        // Given
        final BigDecimal amount = new BigDecimal(amountStr);
        final BigDecimal minAmount = new BigDecimal(minAmountStr);
        final BigDecimal maxAmount = new BigDecimal(maxAmountStr);

        final Application application = ApplicationValidatorTest.createTestApplication(amount);
        final LoanType loanType = ApplicationValidatorTest.createTestLoanType(minAmount, maxAmount);

        // When & Then - Should not throw exception for boundary values
        assertDoesNotThrow(() -> ApplicationValidator.validateLoanAmount(application, loanType),
                "Boundary loan amount should be valid for: " + scenario);
    }

    // ============ EDGE CASE TESTS ============

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.01", "999999999999999999999999999"})
    @DisplayName("Should handle edge case amounts correctly")
    void shouldHandleEdgeCaseAmounts(final String amountStr) {
        // Given
        final BigDecimal amount = new BigDecimal(amountStr);
        final Application application = ApplicationValidatorTest.createTestApplication(amount);
        final LoanType loanType = ApplicationValidatorTest.createTestLoanType(new BigDecimal("1000000"), new BigDecimal("10000000"));

        // When & Then
        if (0 <= amount.compareTo(new BigDecimal("1000000")) && 0 >= amount.compareTo(new BigDecimal("10000000"))) {
            assertDoesNotThrow(() -> ApplicationValidator.validateLoanAmount(application, loanType),
                    "Amount within range should be valid: " + amountStr);
        } else {
            assertThrows(BusinessException.class,
                    () -> ApplicationValidator.validateLoanAmount(application, loanType),
                    "Amount outside range should throw exception: " + amountStr);
        }
    }

    // ============ TEST DATA PROVIDERS ============

    static Stream<Arguments> validUserRoleScenarios() {
        return Stream.of(
                Arguments.of(ApplicationValidatorTest.createTestUser(ValidationMessages.CLIENT_ROLE_ID), ValidationMessages.CLIENT_ROLE_ID,
                        "HU3: Client creating loan application"),
                Arguments.of(ApplicationValidatorTest.createTestUser(ValidationMessages.ADVISOR_ROLE_ID), ValidationMessages.ADVISOR_ROLE_ID,
                        "HU4: Advisor reviewing pending applications"),
                Arguments.of(ApplicationValidatorTest.createTestUser(ValidationMessages.ADVISOR_ROLE_ID), ValidationMessages.ADVISOR_ROLE_ID,
                        "HU5: Advisor evaluating loan applications"),
                Arguments.of(ApplicationValidatorTest.createTestUser(ValidationMessages.ADMIN_ROLE_ID), ValidationMessages.ADMIN_ROLE_ID,
                        "HU7-HU11: Admin generating business reports"),
                Arguments.of(ApplicationValidatorTest.createTestUser(1), 1, "Custom role validation"),
                Arguments.of(ApplicationValidatorTest.createTestUser(999), 999, "Non-standard role ID validation")
        );
    }

    static Stream<Arguments> invalidUserRoleScenarios() {
        return Stream.of(
                Arguments.of(ApplicationValidatorTest.createTestUser(ValidationMessages.CLIENT_ROLE_ID), ValidationMessages.ADVISOR_ROLE_ID,
                        "HU4: Client trying to access advisor functionality"),
                Arguments.of(ApplicationValidatorTest.createTestUser(ValidationMessages.CLIENT_ROLE_ID), ValidationMessages.ADMIN_ROLE_ID,
                        "HU7: Client trying to access admin reports"),
                Arguments.of(ApplicationValidatorTest.createTestUser(ValidationMessages.ADVISOR_ROLE_ID), ValidationMessages.CLIENT_ROLE_ID,
                        "HU3: Advisor trying to create client application"),
                Arguments.of(ApplicationValidatorTest.createTestUser(ValidationMessages.ADVISOR_ROLE_ID), ValidationMessages.ADMIN_ROLE_ID,
                        "HU7: Advisor trying to access admin functionality"),
                Arguments.of(ApplicationValidatorTest.createTestUser(999), ValidationMessages.CLIENT_ROLE_ID,
                        "Unknown role trying to access client functionality"),
                Arguments.of(ApplicationValidatorTest.createTestUser(null), ValidationMessages.CLIENT_ROLE_ID,
                        "User with null role trying to access functionality")
        );
    }

    static Stream<Arguments> validLoanAmountScenarios() {
        return Stream.of(
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("1000000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("500000"), new BigDecimal("2000000")),
                        "HU3: Libre Inversión - Valid middle amount"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("500000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("500000"), new BigDecimal("2000000")),
                        "HU3: Libre Inversión - Minimum amount boundary"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("2000000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("500000"), new BigDecimal("2000000")),
                        "HU3: Libre Inversión - Maximum amount boundary"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("5000000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("1000000"), new BigDecimal("10000000")),
                        "HU3: Educativo - Valid middle amount"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("1000000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("1000000"), new BigDecimal("10000000")),
                        "HU3: Educativo - Minimum amount boundary"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("10000000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("1000000"), new BigDecimal("10000000")),
                        "HU3: Educativo - Maximum amount boundary")
        );
    }

    static Stream<Arguments> invalidLoanAmountScenarios() {
        return Stream.of(
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("400000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("500000"), new BigDecimal("2000000")),
                        "HU3: Libre Inversión - Amount below minimum"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("2500000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("500000"), new BigDecimal("2000000")),
                        "HU3: Libre Inversión - Amount above maximum"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("500000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("1000000"), new BigDecimal("10000000")),
                        "HU3: Educativo - Amount below minimum"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("15000000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("1000000"), new BigDecimal("10000000")),
                        "HU3: Educativo - Amount above maximum"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(BigDecimal.ZERO),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("500000"), new BigDecimal("2000000")),
                        "HU3: Zero amount request"),
                Arguments.of(ApplicationValidatorTest.createTestApplication(new BigDecimal("-1000000")),
                        ApplicationValidatorTest.createTestLoanType(new BigDecimal("500000"), new BigDecimal("2000000")),
                        "HU3: Negative amount request")
        );
    }

    // ============ TEST UTILITY METHODS ============

    private static UserRecord createTestUser(final Integer roleId) {
        return new UserRecord(
                "1",
                "Test",
                "User",
                null, // birthDate
                "test@example.com",
                "12345678",
                "1234567890",
                roleId,
                3000000.0
        );
    }

    private static Application createTestApplication(final BigDecimal amount) {
        return new Application(
                null, // applicationId
                "12345678",
                amount,
                12, // term
                "test@example.com",
                1, // statusId
                1 // loanTypeId
                , null, null, null, null, null, null
        );
    }

    private static LoanType createTestLoanType(final BigDecimal minAmount, final BigDecimal maxAmount) {
        return new LoanType(
                1,
                "Test Loan Type",
                minAmount,
                maxAmount,
                new BigDecimal("12.5"),
                true
        );
    }
}