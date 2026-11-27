package com.ixtom.uds.service;

import java.util.UUID;

public interface ImageStorage {
    boolean isConfigured();

    String upload(UUID id, ImageUpload image) throws Failure;

    void delete(UUID id) throws Failure;

    class Failure extends RuntimeException {
        public Failure() {}

        public Failure(Throwable cause) {
            super(cause);
        }
    }
}
