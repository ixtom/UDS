package com.ixtom.uds.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCompetitionRequest(
    @NotBlank(message = "Enter a title.")
    String title,

    @NotNull(message = "Pick a start date and time.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime start,

    String timezone
) {}
