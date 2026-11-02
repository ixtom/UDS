package com.ixtom.uds.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank(message = "Enter your current password.")
    String currentPassword,

    @NotBlank(message = "Enter a new password.")
    String newPassword,

    @NotBlank(message = "Confirm your new password.")
    String confirmPassword
) {}
