package com.ixtom.uds.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public record ImageUpload(byte[] bytes, String extension, MediaType contentType) {
    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final int MAX_DIMENSION = 4096;

    public static ImageUpload read(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImage("Choose a JPEG or PNG image.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new InvalidImage("Choose an image no larger than 5 MB.");
        }

        try (InputStream bytes = file.getInputStream();
                var stream = new MemoryCacheImageInputStream(bytes)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new InvalidImage("Choose a readable JPEG or PNG image.");
            }

            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("jpeg") && !format.equals("png")) {
                    throw new InvalidImage("Only JPEG and PNG images are supported.");
                }
                reader.setInput(stream, true, true);
                if (reader.getWidth(0) > MAX_DIMENSION || reader.getHeight(0) > MAX_DIMENSION) {
                    throw new InvalidImage("Images must be at most 4096 × 4096 pixels.");
                }
                BufferedImage image = reader.read(0);
                ByteArrayOutputStream normalized = new ByteArrayOutputStream();
                if (!ImageIO.write(image, format, normalized)) {
                    throw new InvalidImage("Choose a readable JPEG or PNG image.");
                }
                if (normalized.size() > MAX_BYTES) {
                    throw new InvalidImage("Choose an image that fits within the 5 MB limit.");
                }
                return format.equals("png")
                        ? new ImageUpload(normalized.toByteArray(), "png", MediaType.IMAGE_PNG)
                        : new ImageUpload(normalized.toByteArray(), "jpg", MediaType.IMAGE_JPEG);
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException ex) {
            throw new InvalidImage("Choose a readable JPEG or PNG image.");
        }
    }

    public static class InvalidImage extends RuntimeException {
        public InvalidImage(String message) {
            super(message);
        }
    }
}
