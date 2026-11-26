package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.UserAuth;

class PreventReusedEmailTest extends IntegrationTest {

    // Story 4, Success row: register with an email nobody has used -> account created
    @Test
    void unusedEmailCreatesAccount() throws Exception {
        mvc.perform(post("/register")
                .param("email", "fresh@example.com")
                .param("username", "fresh_user")
                .param("password", "Passw0rd!")
                .param("cf-turnstile-response", VALID_TURNSTILE_TOKEN)
                .with(csrf()))
            .andExpect(redirectedUrl("/"));

        assertThat(userAuthRepo.findByEmail("fresh@example.com")).isPresent();
    }

    // Story 4, Failure row: register with an email that is already registered -> error: email already in use, no account created
    @Test
    void reusedEmailCreatesNoAccountAndLeavesExistingUnchanged() throws Exception {
        UserAuth existing = createUser("original"); // original@example.com
        String originalHash = existing.getHashedPassword();
        flushAndClear();
        long accountsBefore = userAuthRepo.count();

        mvc.perform(post("/register")
                .param("email", "original@example.com")
                .param("username", "alt_account")
                .param("password", "Different1!")
                .param("cf-turnstile-response", VALID_TURNSTILE_TOKEN)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"))
            .andExpect(model().attributeHasFieldErrorCode("registerRequest", "email", "register.email.already_exists"));

        flushAndClear();
        assertThat(userAuthRepo.count()).isEqualTo(accountsBefore);
        UserAuth after = userAuthRepo.findByEmail("original@example.com").orElseThrow();
        assertThat(after.getHashedPassword()).isEqualTo(originalHash);
        assertThat(after.getProfile().getUsername()).isEqualTo("original");
    }
}
