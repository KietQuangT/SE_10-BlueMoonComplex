package vn.edu.se10.bluemoon.identity.api;

import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;

public record SessionUserResponse(String username, String fullName, String role, Long householdId, boolean mustChangePassword) {
    public static SessionUserResponse from(AuthenticatedUser user) {
        return new SessionUserResponse(user.username(), user.fullName(), user.role(), user.householdId(), user.mustChangePassword());
    }
}
