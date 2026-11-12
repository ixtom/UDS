package com.ixtom.uds.dto;

import com.ixtom.uds.model.Duelist;

public record DuelOpponentQuery(
    Long duelId,
    Duelist opponent
) {}
