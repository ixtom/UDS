package com.ixtom.uds.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CloudinaryImageStorageTest {
    @Test
    void sendsAnAuthenticatedMultipartUploadAndBuildsThePublicUrl() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.cloudinary.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CloudinaryImageStorage storage = new CloudinaryImageStorage("test-cloud", "test", "secret", builder.build());
        UUID id = UUID.randomUUID();
        String publicId = "uds-user-images/" + id;
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/upload"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpzZWNyZXQ="))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(containsString(publicId)))
                .andExpect(content().string(containsString("filename=\"image.png\"")))
                .andRespond(withSuccess("""
                        {"public_id":"%s","version":12345,"format":"png","other":"ignored"}
                        """.formatted(publicId), MediaType.APPLICATION_JSON));

        assertThat(storage.upload(id, new ImageUpload(new byte[] {1, 2, 3}, "png", MediaType.IMAGE_PNG)))
                .isEqualTo("https://res.cloudinary.com/test-cloud/image/upload/v12345/" + publicId + ".png");
        server.verify();
    }

    @Test
    void deletionRequestsCacheInvalidationAndAcceptsAnAlreadyRemovedImage() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.cloudinary.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CloudinaryImageStorage storage = new CloudinaryImageStorage("test-cloud", "test", "secret", builder.build());
        UUID id = UUID.randomUUID();
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/destroy"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpzZWNyZXQ="))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(containsString("public_id=uds-user-images%2F" + id)))
                .andExpect(content().string(containsString("invalidate=true")))
                .andRespond(withSuccess("{\"result\":\"not found\"}", MediaType.APPLICATION_JSON));

        storage.delete(id);
        server.verify();
    }
}
