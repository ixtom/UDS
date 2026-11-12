package com.ixtom.uds.dto;

import com.ixtom.uds.utils.SocialLink;
import com.ixtom.uds.validation.OptionalSocialLink;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// used for both create and edit
public record EditDuelistRequest(
    @NotBlank
    @Size(min = 2, max = 25)
    @Pattern(regexp = "^[\\p{L}\\p{N}_ '\\-]+$")
    String name,

    @NotNull
    @Size(max = 50)
    @Pattern(regexp = "^$|^[\\p{L}\\p{N}_ '\\-]+$")
    String title,

    @NotNull
    @Size(max = 20000, message = "Backstory must be less than 20,000 characters long!")
    String description,

    // TODO: duelist photo

    @NotNull
    @OptionalSocialLink(type = SocialLink.YOUTUBE_VIDEO)
    String demoVideo
) {
    public EditDuelistRequest {
        name = name == null ? null : name.strip();
        title = title == null ? null : title.strip();
        description = description == null ? null : description.strip();
        demoVideo = demoVideo == null ? null : demoVideo.strip();
    }
}
