package com.ixtom.uds.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.ImageAsset;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.repository.DuelistRepository;
import com.ixtom.uds.repository.ImageAssetRepository;
import com.ixtom.uds.repository.UserProfileRepository;

@Service
public class UserImageService {
    private static final Logger log = LoggerFactory.getLogger(UserImageService.class);
    private static final Duration UPLOAD_GRACE_PERIOD = Duration.ofHours(1);
    private static final Duration RETRY_DELAY = Duration.ofMinutes(10);

    private final UserProfileRepository profiles;
    private final DuelistRepository duelists;
    private final ImageAssetRepository assets;
    private final Map<String, ImageStorage> storages;
    private final String activeProvider;
    private final TransactionTemplate transactions;

    public UserImageService(UserProfileRepository profiles, DuelistRepository duelists,
            ImageAssetRepository assets, Map<String, ImageStorage> storages,
            @Value("${uds.images.provider:cloudinary}") String activeProvider,
            PlatformTransactionManager transactionManager) {
        this.profiles = profiles;
        this.duelists = duelists;
        this.assets = assets;
        this.storages = storages;
        this.activeProvider = activeProvider;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public void uploadAvatar(Long accountId, MultipartFile file) {
        if (!profiles.existsById(accountId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        upload(file, image -> setAvatar(accountId, image));
    }

    public void removeAvatar(Long accountId) {
        transactions.executeWithoutResult(status -> setAvatar(accountId, null));
    }

    public void uploadPortrait(Long accountId, Long duelistId, MultipartFile file) {
        if (!duelists.existsByIdAndOwnerId(duelistId, accountId)) {
            throw new ResponseStatusException(duelists.existsById(duelistId) ? HttpStatus.FORBIDDEN : HttpStatus.NOT_FOUND);
        }
        upload(file, image -> setPortrait(accountId, duelistId, image));
    }

    public void removePortrait(Long accountId, Long duelistId) {
        transactions.executeWithoutResult(status -> setPortrait(accountId, duelistId, null));
    }

    private void upload(MultipartFile file, Consumer<ImageAsset> attachToOwner) {
        ImageUpload image = ImageUpload.read(file);
        ImageStorage storage = configuredStorage(activeProvider);

        // Reserve the ID before the network call so crashes and ambiguous upload failures can be cleaned up.
        UUID id = transactions.execute(status -> assets.save(
                new ImageAsset(activeProvider, Instant.now().plus(UPLOAD_GRACE_PERIOD))).getId());
        String url = storage.upload(id, image);
        transactions.executeWithoutResult(status -> {
            ImageAsset asset = assets.findForUpdate(id).orElseThrow(ImageStorage.Failure::new);
            if (asset.isDueForCleanup(Instant.now())) {
                throw new ImageStorage.Failure();
            }
            attachToOwner.accept(asset);
            asset.attach(url);
        });
    }

    private void setAvatar(Long accountId, ImageAsset image) {
        UserProfile profile = profiles.findForUpdate(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        retire(profile.getAvatar());
        profile.setAvatar(image);
    }

    private void setPortrait(Long accountId, Long duelistId, ImageAsset image) {
        Duelist duelist = duelists.findForUpdate(duelistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (duelist.getOwner() == null || !accountId.equals(duelist.getOwner().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        retire(duelist.getPortrait());
        duelist.setPortrait(image);
        duelist.setPhoto(null);
    }

    private void retire(ImageAsset formerImage) {
        if (formerImage != null) {
            formerImage.scheduleCleanup(Instant.now());
        }
    }

    private ImageStorage configuredStorage(String provider) {
        ImageStorage storage = storages.get(provider);
        if (storage == null || !storage.isConfigured()) {
            throw new ImageStorage.Failure();
        }
        return storage;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void cleanUpImages() {
        Instant now = Instant.now();
        for (ImageAsset candidate : assets.findTop20ByCleanupAfterLessThanEqualOrderByCleanupAfterAsc(now)) {
            try {
                transactions.executeWithoutResult(status ->
                    assets.findForUpdate(candidate.getId())
                          .filter(asset -> asset.isDueForCleanup(now))
                          .ifPresent(this::deleteImage));
            } catch (DataAccessException | TransactionException ex) {
                log.warn("Could not update cleanup state for image {}; will retry.", candidate.getId());
            }
        }
    }

    private void deleteImage(ImageAsset asset) {
        try {
            configuredStorage(asset.getProvider()).delete(asset.getId());
            assets.delete(asset);
        } catch (ImageStorage.Failure ex) {
            asset.scheduleCleanup(Instant.now().plus(RETRY_DELAY));
            log.warn("Could not remove image {} from {}; will retry.", asset.getId(), asset.getProvider());
        }
    }
}
