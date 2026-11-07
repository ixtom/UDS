package com.ixtom.uds.dto;

import jakarta.validation.constraints.Size;

public record EditProfileRequest(
        @Size(max = 2000, message = "Bio must be 2000 characters or fewer.") String bio,
        @Size(max = 2048, message = "Link must be 2048 characters or fewer.") String youtubeUrl,
        @Size(max = 2048, message = "Link must be 2048 characters or fewer.") String instagramUrl) {

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
