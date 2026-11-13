package com.ixtom.uds.dto;

import com.ixtom.uds.utils.SocialLink;
import com.ixtom.uds.validation.OptionalSocialLink;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditDuelRequest(
    @NotNull
    @OptionalSocialLink(type = SocialLink.YOUTUBE_VIDEO)
    String video,

    /* shared data */

    String banner, // TODO: check for vaild Cloudinary url

    @Size(max = 256)
    String title,

    @Size(max = 5000, message = "Description. must be 5000 characters or less.")
    String description
) {}
