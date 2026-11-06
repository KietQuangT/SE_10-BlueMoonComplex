package vn.edu.se10.bluemoon.identity.api;
public record AccountResponse(long id, String username, String fullName, String email, String role, Long householdId, String emailStatus) {}
