package com.ixtom.uds;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.Cookie;

class LoginTest extends IntegrationTest {

    // Story 2, Success row: log in with a valid user account -> sent to the user landing page
    @Test
    void validLoginGoesToLandingPage() throws Exception {
        createUser("benny");

        Cookie session = login("benny@example.com", PASSWORD); // asserts redirect to "/"

        mvc.perform(get("/dashboard").cookie(session))
            .andExpect(status().isOk());
    }

    // Story 2, Failure row: wrong email or password -> error shown on login page
    @Test
    void wrongPasswordShowsErrorOnLoginPage() throws Exception {
        createUser("benny");

        mvc.perform(post("/login")
                .param("email", "benny@example.com")
                .param("password", "WrongPass1!")
                .with(csrf()))
            .andExpect(redirectedUrl("/login?error"));

        mvc.perform(get("/login").param("error", ""))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Wrong email or password")));
    }

    // Story 2, Failure row: wrong email or password -> error shown on login page
    @Test
    void unknownEmailShowsErrorOnLoginPage() throws Exception {
        mvc.perform(post("/login")
                .param("email", "nobody@example.com")
                .param("password", PASSWORD)
                .with(csrf()))
            .andExpect(redirectedUrl("/login?error"));
    }

    // Story 3, Success row: log in, close the tab, reopen the site -> still logged in, landing page shown
    @Test
    void sessionCookieKeepsUserLoggedIn() throws Exception {
        createUser("benny");
        Cookie session = login("benny@example.com", PASSWORD);

        // a fresh request carrying only the stored cookie, as when the tab is reopened
        mvc.perform(get("/").cookie(session))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("logout")));
        mvc.perform(get("/dashboard").cookie(session))
            .andExpect(status().isOk());
    }

    // Story 3, Failure row: log out, close the browser, reopen the site -> asked to log in again
    @Test
    void afterLogoutUserMustLogInAgain() throws Exception {
        createUser("benny");
        Cookie session = login("benny@example.com", PASSWORD);

        mvc.perform(post("/logout").cookie(session).with(csrf()))
            .andExpect(status().is3xxRedirection());

        mvc.perform(get("/dashboard").cookie(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }
}
