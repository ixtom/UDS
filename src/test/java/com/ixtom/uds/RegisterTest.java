package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MvcResult;

import com.ixtom.uds.model.UserAuth;

import jakarta.servlet.http.Cookie;

class RegisterTest extends IntegrationTest {

    // Story 1, Success row: enter a unique username, email, valid password -> account created & logged in
    @Test
    void uniqueDetailsCreateAccountAndLogIn() throws Exception {
        MvcResult result = mvc.perform(post("/register")
                .param("email", "benny@example.com")
                .param("username", "benny")
                .param("password", "Passw0rd!")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andReturn();

        UserAuth saved = userAuthRepo.findByEmail("benny@example.com").orElseThrow();
        assertThat(saved.getProfile().getUsername()).isEqualTo("benny");
        assertThat(passwordEncoder.matches("Passw0rd!", saved.getHashedPassword())).isTrue();

        // logged in: the session from registration can open a page that requires login
        Cookie session = result.getResponse().getCookie("SESSION");
        assertThat(session).isNotNull();
        mvc.perform(get("/dashboard").cookie(session))
            .andExpect(status().isOk());
    }

    // Story 1, Failure row: leave a field empty -> error shown, no account created
    @ParameterizedTest(name = "empty {0}")
    @CsvSource({
        "email,    '',                 benny,  Passw0rd!",
        "username, benny@example.com,  '',     Passw0rd!",
        "password, benny@example.com,  benny,  ''",
    })
    void emptyFieldIsRejected(String emptyField, String email, String username, String password) throws Exception {
        long accountsBefore = userAuthRepo.count();

        mvc.perform(post("/register")
                .param("email", email)
                .param("username", username)
                .param("password", password)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"))
            .andExpect(model().attributeHasFieldErrors("registerRequest", emptyField));

        assertThat(userAuthRepo.count()).isEqualTo(accountsBefore);
    }

    // Story 1, Failure row: use an email that is already registered -> error: email already in use
    @Test
    void duplicateEmailIsRejected() throws Exception {
        createUser("existing"); // existing@example.com

        mvc.perform(post("/register")
                .param("email", "existing@example.com")
                .param("username", "newcomer")
                .param("password", "Passw0rd!")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"))
            .andExpect(model().attributeHasFieldErrorCode("registerRequest", "email", "taken"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("An account with that email already exists.")));

        assertThat(userProfileRepo.findByUsername("newcomer")).isEmpty();
    }
}
