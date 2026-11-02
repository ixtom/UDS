package com.ixtom.uds.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 2, max = 25)
    @Pattern(regexp = "^[a-z0-9_]{2,25}$")
    String username,

    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$")
    String password
) {
    public RegisterRequest {
        email = email == null ? null : email.strip();
        username = username == null ? null : username.strip();
    }
}
