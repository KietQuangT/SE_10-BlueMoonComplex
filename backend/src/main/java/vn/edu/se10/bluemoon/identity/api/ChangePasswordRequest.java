package vn.edu.se10.bluemoon.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(@NotBlank @Size(max = 1024) String currentPassword,
                                    @NotBlank @Size(max = 1024) String newPassword) {
    @Override public String toString() { return "ChangePasswordRequest[redacted]"; }
}
