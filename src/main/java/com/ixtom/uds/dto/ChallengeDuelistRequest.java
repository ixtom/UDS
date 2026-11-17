package com.ixtom.uds.dto;

import jakarta.validation.constraints.NotNull;

public record ChallengeDuelistRequest(
    @NotNull
    Long duelistId
) {
}
