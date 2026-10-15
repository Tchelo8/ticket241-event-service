package com.tchelo.event_service_ticket241.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

// Identité de l'appelant, telle que la gateway l'a extraite du JWT et transmise en headers
// (X-User-Id, X-User-Roles, X-User-Structure-Id). Les rôles sont comparés exactement, jamais
// par "contains" sur la chaîne brute.
public record UserContext(String userId, Set<String> roles, Long structureId) {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_STRUCTURE_ADMIN = "ROLE_STRUCTURE_ADMIN";

    public static UserContext of(String userId, String rolesHeader, String structureIdHeader) {
        Set<String> roles = rolesHeader == null ? Set.of() : Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        Long structureId = null;
        if (structureIdHeader != null && !structureIdHeader.isBlank()) {
            try {
                structureId = Long.valueOf(structureIdHeader.trim());
            } catch (NumberFormatException ignored) {
                // header malformé : traité comme absent
            }
        }
        return new UserContext(userId, roles, structureId);
    }

    public boolean isAdmin() {
        return roles.contains(ROLE_ADMIN);
    }

    // Admin de structure rattaché à la structure donnée
    public boolean isAdminOfStructure(Long id) {
        return roles.contains(ROLE_STRUCTURE_ADMIN) && structureId != null && structureId.equals(id);
    }

    // Admin général, ou admin de cette structure
    public boolean canManageStructure(Long id) {
        return isAdmin() || isAdminOfStructure(id);
    }
}
