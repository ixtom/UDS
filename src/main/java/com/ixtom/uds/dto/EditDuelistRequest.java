package com.ixtom.uds.dto;

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

    // TODO: refactor this into a custom annotation
    @NotNull
    @Pattern(regexp = "^$|" + NewDuelRequest.YOUTUBE_URL_REGEX, message = "Enter a valid YouTube link, like https://www.youtube.com/watch?v=anc123XYZ_-")
    String demoVideo
) {
    public EditDuelistRequest {
        name = name == null ? null : name.strip();
        title = title == null ? null : title.strip();
        description = description == null ? null : description.strip();
        demoVideo = demoVideo == null ? null : demoVideo.strip();
    }
}
