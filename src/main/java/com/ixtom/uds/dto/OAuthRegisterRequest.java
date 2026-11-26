package com.ixtom.uds.dto;

import com.ixtom.uds.validation.ValidCfTurnstile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@ValidCfTurnstile
public record OAuthRegisterRequest(
    @NotNull
    @Email(message = "Enter a valid email address.")
    String email,

    @NotBlank(message = "Enter a username.")
    @Pattern (regexp = "^[a-z0-9_]{2,25}$",
        message = "Username must be 2 to 25 characters, using only lowercase letters, numbers, and underscores.")
    String username
){
    public OAuthRegisterRequest {
        email = email == null ? null : email.strip();
    }
}
