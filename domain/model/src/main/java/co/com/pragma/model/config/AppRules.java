package co.com.pragma.model.config;

import java.util.List;

public record AppRules(
        Integer clientRoleId,
        Integer pendingStatusId,
        List<Integer> terminalStatusIds
) {
}