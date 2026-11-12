package com.ixtom.uds.dto;

import com.ixtom.uds.utils.SocialLink;
import com.ixtom.uds.validation.OptionalSocialLink;

import jakarta.validation.constraints.Size;

public record EditProfileRequest(
    @Size(max = 2000, message = "Bio must be 2000 characters or fewer.")
    String bio,

    @Size(max = 2048, message = "Link must be 2048 characters or fewer.")
    @OptionalSocialLink(type = SocialLink.YOUTUBE_CHANNEL)
    String youtubeUrl,

    @Size(max = 2048, message = "Link must be 2048 characters or fewer.")
    @OptionalSocialLink(type = SocialLink.INSTAGRAM_PROFILE)
    String instagramUrl
) {
    public EditProfileRequest {
        if (bio == null) {
            bio = "";
        } else {
            bio = bio.strip();
        }
        if (youtubeUrl == null) {
            youtubeUrl = "";
        } else {
            youtubeUrl = youtubeUrl.strip();
        }
        if (instagramUrl == null) {
            instagramUrl = "";
        } else {
            instagramUrl = instagramUrl.strip();
        }
        // Can add more website URL types
    }
}
