package co.com.pragma.model.config;

import java.util.List;

/**
 * A simple data holder for business rules, defined in the domain model.
 * This allows the UseCase to depend on it without knowing about any
 * specific framework or configuration mechanism.
 */
public record AppRules(
        Integer clientRoleId,
        Integer pendingStatusId,
        List<Integer> terminalStatusIds
) {
}