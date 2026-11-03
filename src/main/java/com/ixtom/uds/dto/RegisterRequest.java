package com.ixtom.uds.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Enter your email address.")
    @Email(message = "Enter a valid email address, like name@example.com.")
    String email,

    @NotBlank(message = "Enter a username.")
    @Pattern(regexp = "^[a-z0-9_]{2,25}$", 
            message = "Username must be 2 to 25 characters, using only lowercase letters, numbers, and underscores.")
    String username,

    @NotBlank(message = "Enter a password")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$", 
            message = "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a symbol.")
    String password
) {
    public RegisterRequest {
        email = email == null ? null : email.strip();
        username = username == null ? null : username.strip();
    }
}
