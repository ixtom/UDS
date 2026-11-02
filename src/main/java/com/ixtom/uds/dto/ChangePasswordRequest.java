package com.ixtom.uds.dto;

public record ChangePasswordRequest(
    String currentPassword,
    String newPassword,
    String confirmPassword
) {
    public ChangePasswordRequest {
        if (currentPassword == null) {
            currentPassword = "";
        }
        if (newPassword == null) {
            newPassword = "";
        }
        if (confirmPassword == null) {
            confirmPassword = "";
        }
    }
}
