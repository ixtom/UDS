package com.ixtom.uds.dto;

import com.ixtom.uds.validation.ValidCfTurnstile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidCfTurnstile
public record LoginRequest(
    @NotNull
    @Email(message = "Enter a valid email address.")
    String email,

    @NotBlank(message = "Enter a password")
    String password
){
    public LoginRequest {
        email = email == null ? null : email.strip();
    }
}
