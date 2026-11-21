package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.UserAuth;

import jakarta.servlet.http.Cookie;

class ChangePasswordTest extends IntegrationTest {

    private static final String NEW_PASSWORD = "NewPassw0rd!";

    private void changePassword(Cookie session) throws Exception {
        mvc.perform(post("/change-password")
                .cookie(session)
                .param("currentPassword", PASSWORD)
                .param("newPassword", NEW_PASSWORD)
                .param("confirmPassword", NEW_PASSWORD)
                .param("cf-turnstile-response", VALID_TURNSTILE_TOKEN)
                .with(csrf()))
            .andExpect(redirectedUrl("/login?passwordChanged"));
    }

    // Story 8, Success row: enter the correct current password and a valid new password -> password updated, success message
    @Test
    void correctCurrentPasswordUpdatesPassword() throws Exception {
        UserAuth user = createUser("jayden");
        Cookie session = login("jayden@example.com", PASSWORD);

        changePassword(session);

        flushAndClear();
        UserAuth saved = userAuthRepo.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(NEW_PASSWORD, saved.getHashedPassword())).isTrue();

        mvc.perform(get("/login").param("passwordChanged", ""))
            .andExpect(content().string(containsString("Password changed.")));
    }

    // Story 8, Success row: log out and log in with the new password -> login works
    @Test
    void newPasswordWorksForLogin() throws Exception {
        createUser("jayden");
        changePassword(login("jayden@example.com", PASSWORD));

        login("jayden@example.com", NEW_PASSWORD); // asserts redirect to "/"
    }

    // Story 8, Failure row: log in with the old password after changing it -> error: invalid username or password
    @Test
    void oldPasswordFailsAfterChange() throws Exception {
        createUser("jayden");
        changePassword(login("jayden@example.com", PASSWORD));

        mvc.perform(post("/login")
                .param("email", "jayden@example.com")
                .param("password", PASSWORD)
                .param("cf-turnstile-response", VALID_TURNSTILE_TOKEN)
                .with(csrf()))
            .andExpect(content().string(containsString("Invalid email or password")));
    }
}
