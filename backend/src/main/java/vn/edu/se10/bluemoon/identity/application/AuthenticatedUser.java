package vn.edu.se10.bluemoon.identity.application;

import java.security.Principal;

public record AuthenticatedUser(long userId, String username, String fullName, String role, Long householdId, boolean mustChangePassword)
        implements Principal, java.io.Serializable {
    @Override public String getName() { return username; }
}
