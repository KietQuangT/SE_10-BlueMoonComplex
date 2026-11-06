package vn.edu.se10.bluemoon.identity.application;
public record CreatedAccount(long id, String username, String fullName, String email, String role, Long householdId) {}
