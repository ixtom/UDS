package com.ixtom.uds.dto;

import jakarta.validation.constraints.NotNull;

public record JoinCompetitionRequest(
    @NotNull
    Long duelistId
) {
}
