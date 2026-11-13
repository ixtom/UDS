package com.ixtom.uds.dto;

import com.ixtom.uds.utils.SocialLink;
import com.ixtom.uds.validation.OptionalSocialLink;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NewDuelRequest(
    @NotBlank
    @OptionalSocialLink(type = SocialLink.YOUTUBE_VIDEO)
    String url1,

    @NotBlank
    @OptionalSocialLink(type = SocialLink.YOUTUBE_VIDEO)
    String url2,

    @NotNull(message = "Enter a round number.")
    @Min(value = 1, message = "round number must be 1 or higher.")
    Integer round,

    @Size(max = 5000, message = "Description. must be 5000 characters or less.")
    String description,

    String duelBanner
) {}
