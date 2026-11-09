package com.ixtom.uds.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record DiscordUserDataResponse (
    long id,
    @NotNull
    String username,

    // Nullable
    @JsonProperty("global_name")
    String globalName,
    String avatar,
    Boolean verified,
    String email
) {}
