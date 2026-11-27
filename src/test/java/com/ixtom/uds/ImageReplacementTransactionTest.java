package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.ixtom.uds.model.ImageAsset;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuth.Role;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.repository.ImageAssetRepository;
import com.ixtom.uds.repository.UserAuthRepository;
import com.ixtom.uds.repository.UserProfileRepository;
import com.ixtom.uds.service.CloudinaryImageStorage;
import com.ixtom.uds.service.UserImageService;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:uds-image-writes;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")
class ImageReplacementTransactionTest {
    @Autowired private UserImageService images;
    @Autowired private UserAuthRepository accounts;
    @Autowired private UserProfileRepository profiles;
    @Autowired private ImageAssetRepository assets;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockitoBean private CloudinaryImageStorage storage;

    @BeforeEach
    void configureStorage() {
        when(storage.isConfigured()).thenReturn(true);
        when(storage.upload(any(), any())).thenAnswer(call -> "https://images.example.test/" + call.getArgument(0));
    }

    @Test
    void failedDatabaseWriteKeepsPreviousImageAndLeavesTheUploadForCleanup() throws Exception {
        Long account = account("rollback");
        var file = UserImageTest.image("png", 8, 8);
        images.uploadAvatar(account, file);
        UUID original = avatarId(account);

        when(storage.upload(any(), any())).thenReturn("https://images.example.test/" + "a".repeat(2048));
        assertThatThrownBy(() -> images.uploadAvatar(account, file)).hasRootCauseInstanceOf(SQLException.class);

        var uploadedIds = ArgumentCaptor.forClass(UUID.class);
        verify(storage, times(2)).upload(uploadedIds.capture(), any());
        UUID rejected = uploadedIds.getAllValues().getLast();
        assertThat(avatarId(account)).isEqualTo(original);
        assertThat(assets.findById(original).orElseThrow().isDueForCleanup(Instant.MAX)).isFalse();
        ImageAsset pending = assets.findById(rejected).orElseThrow();
        assertThat(pending.getUrl()).isNull();
        assertThat(pending.isDueForCleanup(Instant.MAX)).isTrue();
    }

    @Test
    void concurrentReplacementsLeaveExactlyOneOfTheUploadedImagesAttached() throws Exception {
        Long account = account("concurrent");
        var file = UserImageTest.image("png", 8, 8);
        images.uploadAvatar(account, file);
        UUID original = avatarId(account);
        CyclicBarrier simultaneousUploads = new CyclicBarrier(2);
        when(storage.upload(any(), any())).thenAnswer(call -> {
            simultaneousUploads.await(10, TimeUnit.SECONDS);
            return "https://images.example.test/" + call.getArgument(0);
        });

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> images.uploadAvatar(account, file));
            var second = executor.submit(() -> images.uploadAvatar(account, file));
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
        }

        var uploadedIds = ArgumentCaptor.forClass(UUID.class);
        verify(storage, times(3)).upload(uploadedIds.capture(), any());
        UUID current = avatarId(account);
        assertThat(current).isNotEqualTo(original);
        assertThat(assets.findAllById(uploadedIds.getAllValues()))
                .filteredOn(asset -> !asset.isDueForCleanup(Instant.MAX))
                .extracting(ImageAsset::getId).containsExactly(current);
    }

    private Long account(String username) {
        UserAuth account = new UserAuth(username + "@example.test", "unused-by-this-test", Role.USER);
        UserProfile profile = new UserProfile(null, username);
        account.setProfile(profile);
        profile.setUserAuth(account);
        return accounts.save(account).getId();
    }

    private UUID avatarId(Long account) {
        return new TransactionTemplate(transactionManager).execute(status ->
                profiles.findById(account).orElseThrow().getAvatar().getId());
    }
}
