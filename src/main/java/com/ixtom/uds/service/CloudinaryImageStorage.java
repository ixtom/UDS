package com.ixtom.uds.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service("cloudinary")
public class CloudinaryImageStorage implements ImageStorage {
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final RestClient client;

    @Autowired
    public CloudinaryImageStorage(
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:}") String apiSecret) {
        this(cloudName, apiKey, apiSecret, cloudinaryClient());
    }

    CloudinaryImageStorage(String cloudName, String apiKey, String apiSecret, RestClient client) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.client = client;
    }

    @Override
    public boolean isConfigured() {
        return cloudName.matches("[a-zA-Z0-9_-]+") && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    @Override
    public String upload(UUID id, ImageUpload image) {
        String publicId = publicId(id);
        LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("public_id", publicId);
        form.add("overwrite", "false");
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentDispositionFormData("file", "image." + image.extension());
        fileHeaders.setContentType(image.contentType());
        form.add("file", new HttpEntity<>(image.bytes(), fileHeaders));

        UploadResponse result = send("upload", MediaType.MULTIPART_FORM_DATA, form, UploadResponse.class);
        if (result == null || !publicId.equals(result.publicId()) || result.version() <= 0
                || !(image.extension().equals(result.format())
                     || ("jpg".equals(image.extension()) && "jpeg".equals(result.format())))) {
            throw new Failure();
        }
        return "https://res.cloudinary.com/" + cloudName + "/image/upload/v"
                + result.version() + "/" + publicId + "." + result.format();
    }

    @Override
    public void delete(UUID id) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("public_id", publicId(id));
        form.add("invalidate", "true");

        DeleteResponse response = send("destroy", MediaType.APPLICATION_FORM_URLENCODED, form, DeleteResponse.class);
        if (response == null || (!"ok".equals(response.result()) && !"not found".equals(response.result()))) {
            throw new Failure();
        }
    }

    private <T> T send(String action, MediaType type, Object form, Class<T> responseType) {
        if (!isConfigured()) {
            throw new Failure();
        }
        try {
            return client.post()
                    .uri("/v1_1/{cloudName}/image/{action}", cloudName, action)
                    .headers(headers -> headers.setBasicAuth(apiKey, apiSecret))
                    .contentType(type)
                    .body(form)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientException ex) {
            throw new Failure(ex);
        }
    }

    private static String publicId(UUID id) {
        return "uds-user-images/" + id;
    }

    private static RestClient cloudinaryClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder().baseUrl("https://api.cloudinary.com").requestFactory(factory).build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UploadResponse(@JsonProperty("public_id") String publicId, long version, String format) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DeleteResponse(String result) {}
}
