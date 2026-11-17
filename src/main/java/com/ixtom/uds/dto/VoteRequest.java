package com.ixtom.uds.dto;


public record VoteRequest(
    Long cat1winner,
    Long cat2winner,
    Long cat3winner,
    Long cat4winner,
    Long cat5winner
) {}
