package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.ImageAsset;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.repository.ImageAssetRepository;
import com.ixtom.uds.service.CloudinaryImageStorage;
import com.ixtom.uds.service.ImageStorage;
import com.ixtom.uds.service.UserImageService;

class UserImageTest extends IntegrationTest {
    private static final String PROFILE_IMAGE = "/account/profile/image";
    private static final String EDIT_PROFILE = "/account/profile/edit";

    @MockitoBean private CloudinaryImageStorage storage;
    @Autowired private ImageAssetRepository assets;
    @Autowired private UserImageService images;

    @BeforeEach
    void configureImageStorage() {
        when(storage.isConfigured()).thenReturn(true);
        when(storage.upload(any(), any())).thenAnswer(call -> imageUrl(call.getArgument(0)));
    }

    @Test
    void avatarCanBeUploadedReplacedAndRemoved() throws Exception {
        UserAuth owner = createUser("image_owner");
        mvc.perform(get(EDIT_PROFILE).with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"" + PROFILE_IMAGE + "\"")))
                .andExpect(content().string(containsString("enctype=\"multipart/form-data\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        mvc.perform(multipart(PROFILE_IMAGE).file(image("png", 8, 8)).with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl(EDIT_PROFILE))
                .andExpect(flash().attribute("imageSuccess", "Profile picture saved."));
        UUID first = avatarId(owner);
        mvc.perform(post(EDIT_PROFILE).param("bio", "Updated bio").with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl("/profile/image_owner"));
        mvc.perform(get("/profile/image_owner"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Updated bio")))
                .andExpect(content().string(containsString(imageUrl(first))));

        mvc.perform(multipart(PROFILE_IMAGE).file(image("jpg", 8, 8)).with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl(EDIT_PROFILE));
        UUID replacement = avatarId(owner);
        assertThat(replacement).isNotEqualTo(first);
        mvc.perform(get(EDIT_PROFILE).with(as(owner)))
                .andExpect(content().string(containsString(imageUrl(replacement))))
                .andExpect(content().string(containsString("action=\"" + PROFILE_IMAGE + "/remove\"")));

        images.cleanUpImages();
        verify(storage).delete(first);
        verify(storage, never()).delete(replacement);
        assertThat(assets.findById(first)).isEmpty();

        mvc.perform(post(PROFILE_IMAGE + "/remove").with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl(EDIT_PROFILE));
        flushAndClear();
        mvc.perform(get("/profile/image_owner"))
                .andExpect(content().string(containsString("Default profile picture")))
                .andExpect(content().string(not(containsString(imageUrl(replacement)))));
    }

    @Test
    void duelistPortraitIsPublicAndLegacyPhotoCanBeReplaced() throws Exception {
        UserAuth owner = createUser("image_owner");
        Duelist ash = createDuelist(owner, "Ash");
        ash.setPhoto("https://images.example.test/older.png");
        flushAndClear();
        String duelistPath = "/duelist/" + ash.getId();

        mvc.perform(get(duelistPath))
                .andExpect(content().string(containsString("https://images.example.test/older.png")));
        mvc.perform(get(duelistPath + "/edit").with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"" + duelistPath + "/image\"")));

        mvc.perform(multipart(duelistPath + "/image").file(image("png", 8, 8)).with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl(duelistPath + "/edit"));
        flushAndClear();
        Duelist saved = duelistRepo.findById(ash.getId()).orElseThrow();
        String portrait = saved.getPortraitUrl();
        assertThat(portrait).startsWith("https://images.example.test/");
        assertThat(saved.getPhoto()).isNull();
        mvc.perform(post(duelistPath + "/edit").param("name", "Ash").param("title", "")
                .param("description", "Updated backstory").param("demoVideo", "").with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl(duelistPath));
        mvc.perform(get(duelistPath))
                .andExpect(content().string(containsString("Updated backstory")))
                .andExpect(content().string(containsString(portrait)));

        mvc.perform(post(duelistPath + "/image/remove").with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl(duelistPath + "/edit"));
        flushAndClear();
        mvc.perform(get(duelistPath))
                .andExpect(content().string(containsString("Default duelist portrait")))
                .andExpect(content().string(not(containsString(portrait))));
    }

    @Test
    void uploadsRequireAuthenticationCsrfAndDuelistOwnership() throws Exception {
        UserAuth owner = createUser("image_owner");
        UserAuth other = createUser("other_user");
        Duelist ash = createDuelist(owner, "Ash");
        String portrait = "/duelist/" + ash.getId() + "/image";

        mvc.perform(multipart(PROFILE_IMAGE).file(image("png", 8, 8)).with(csrf()))
                .andExpect(redirectedUrl("/login"));
        mvc.perform(multipart(portrait).file(image("png", 8, 8)).with(csrf()))
                .andExpect(redirectedUrl("/login"));
        mvc.perform(multipart(PROFILE_IMAGE).file(image("png", 8, 8)).with(as(owner)))
                .andExpect(status().isForbidden());
        mvc.perform(multipart(portrait).file(image("png", 8, 8)).with(as(other)).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post(portrait + "/remove").with(as(other)).with(csrf()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(storage);
    }

    @Test
    void invalidAndOversizedImagesAreRejectedBeforeStorage() throws Exception {
        UserAuth owner = createUser("image_owner");
        MockMultipartFile invalid = new MockMultipartFile("image", "fake.png", "image/png", new byte[] {1, 2, 3});
        MockMultipartFile tooBig = new MockMultipartFile("image", "big.png", "image/png", new byte[5 * 1024 * 1024 + 1]);

        mvc.perform(multipart(PROFILE_IMAGE).file(invalid).with(as(owner)).with(csrf()))
                .andExpect(flash().attribute("imageError", "Choose a readable JPEG or PNG image."));
        mvc.perform(multipart(PROFILE_IMAGE).file(tooBig).with(as(owner)).with(csrf()))
                .andExpect(flash().attribute("imageError", "Choose an image no larger than 5 MB."));
        mvc.perform(multipart(PROFILE_IMAGE).file(image("png", 4097, 1)).with(as(owner)).with(csrf()))
                .andExpect(flash().attribute("imageError", "Images must be at most 4096 × 4096 pixels."));
        verifyNoInteractions(storage);
        assertThat(assets.count()).isZero();
    }

    @Test
    void storageFailureLeavesExistingImageAndTracksUncertainUpload() throws Exception {
        UserAuth owner = createUser("image_owner");
        mvc.perform(multipart(PROFILE_IMAGE).file(image("png", 8, 8)).with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl(EDIT_PROFILE));
        UUID first = avatarId(owner);
        when(storage.upload(any(), any())).thenThrow(new ImageStorage.Failure());

        mvc.perform(multipart(PROFILE_IMAGE).file(image("png", 8, 8)).with(as(owner)).with(csrf()))
                .andExpect(redirectedUrl(EDIT_PROFILE))
                .andExpect(flash().attribute("imageError", "Image storage is unavailable. Try again later."));
        assertThat(avatarId(owner)).isEqualTo(first);
        assertThat(assets.findAll()).hasSize(2);
        images.cleanUpImages();
        verify(storage, never()).delete(any());
    }

    @Test
    void cleanupRetriesFailedDeletionWithoutDeletingAnAttachedImage() {
        ImageAsset retired = assets.save(new ImageAsset("cloudinary", Instant.EPOCH));
        ImageAsset attached = assets.save(new ImageAsset("cloudinary", Instant.EPOCH));
        attached.attach(imageUrl(attached.getId()));
        flushAndClear();

        doThrow(new ImageStorage.Failure()).doNothing().when(storage).delete(eq(retired.getId()));
        images.cleanUpImages();
        assertThat(assets.findById(retired.getId())).isPresent();
        assertThat(assets.findById(retired.getId()).orElseThrow().isDueForCleanup(Instant.now())).isFalse();
        assets.findById(retired.getId()).orElseThrow().scheduleCleanup(Instant.EPOCH);
        flushAndClear();

        images.cleanUpImages();
        assertThat(assets.findById(retired.getId())).isEmpty();
        verify(storage, never()).delete(attached.getId());
    }

    private UUID avatarId(UserAuth owner) {
        flushAndClear();
        return userProfileRepo.findById(owner.getId()).orElseThrow().getAvatar().getId();
    }

    private static String imageUrl(UUID id) {
        return "https://images.example.test/" + id;
    }

    static MockMultipartFile image(String format, int width, int height) throws IOException {
        int type = format.equals("png") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, type), format, bytes);
        return new MockMultipartFile("image", "picture." + format, "application/octet-stream", bytes.toByteArray());
    }
}
