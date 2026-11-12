package com.ixtom.uds.dto;

import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Duelist;

public record UserDuelQuery(
    Duel duel,
    Duelist duelist,
    Duelist opponent
) {}
