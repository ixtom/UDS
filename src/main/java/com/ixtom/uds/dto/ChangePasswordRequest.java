package com.ixtom.uds.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
    @NotBlank(message = "Enter your current password.")
    String currentPassword,

    @NotBlank(message = "Enter a new password.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$", 
            message = "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a symbol.")
    String newPassword,

    @NotBlank(message = "Confirm your new password.")
    String confirmPassword
) {}
