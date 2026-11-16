package com.ixtom.uds.dto;

import java.time.Instant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCompetitionRequest(
    @NotBlank(message = "Enter a title.")
    @Size(max = 255, message = "Title must be 255 characters or less.")
    String title,

    @NotNull(message = "Pick a start date and time.") Instant start,

    @NotNull(message = "Enter the number of rounds.")
    @Min(value = 2, message = "Must have at least two rounds")
    Integer rounds
) {}
