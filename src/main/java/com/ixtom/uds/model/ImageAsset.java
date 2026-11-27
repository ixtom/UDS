package com.ixtom.uds.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "image_asset")
public class ImageAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String provider;

    @Column(length = 2048)
    private String url;

    // Pending and retired assets have a cleanup date. An attached asset does not.
    private Instant cleanupAfter;

    protected ImageAsset() {}

    public ImageAsset(String provider, Instant cleanupAfter) {
        this.provider = provider;
        this.cleanupAfter = cleanupAfter;
    }

    public UUID getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDueForCleanup(Instant now) {
        return cleanupAfter != null && !cleanupAfter.isAfter(now);
    }

    public void attach(String url) {
        this.url = url;
        cleanupAfter = null;
    }

    public void scheduleCleanup(Instant when) {
        cleanupAfter = when;
    }
}
