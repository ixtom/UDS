package com.ixtom.uds;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.CompetitionEntry;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuth.Role;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.repository.CompetitionEntryRepository;
import com.ixtom.uds.repository.CompetitionRepository;
import com.ixtom.uds.repository.DuelRepository;
import com.ixtom.uds.repository.DuelistRepository;
import com.ixtom.uds.repository.UserAuthRepository;
import com.ixtom.uds.repository.UserProfileRepository;
import com.ixtom.uds.service.TurnstileService;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

/**
 * Shared setup for story tests: the full app on an in-memory H2 database (see
 * src/test/resources/application.properties). Every test runs in its own transaction
 * that is rolled back afterwards, so tests never see each other's data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTest {

    protected static final String PASSWORD = "Passw0rd!";

    protected static final String VALID_TURNSTILE_TOKEN = "valid-token";
    protected static final String INVALID_TURNSTILE_TOKEN = "invalid-token";

    @MockitoBean protected TurnstileService turnstileService;

    @Autowired protected MockMvc mvc;
    @Autowired protected EntityManager em;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected UserAuthRepository userAuthRepo;
    @Autowired protected UserProfileRepository userProfileRepo;
    @Autowired protected DuelistRepository duelistRepo;
    @Autowired protected DuelRepository duelRepo;
    @Autowired protected CompetitionRepository competitionRepo;
    @Autowired protected CompetitionEntryRepository competitionEntryRepo;

    /** Creates a USER account with email {username}@example.com and password {@link #PASSWORD}. */
    protected UserAuth createUser(String username) {
        return createUser(username, Role.USER);
    }

    protected UserAuth createUser(String username, Role role) {
        UserAuth user = new UserAuth(username + "@example.com", passwordEncoder.encode(PASSWORD), role);
        UserProfile profile = new UserProfile(null, username);
        user.setProfile(profile);
        profile.setUserAuth(user);
        return userAuthRepo.save(user);
    }

    /** Runs a request as the given signed-in user. */
    protected RequestPostProcessor as(UserAuth user) {
        return user(new UserAuthPrincipal(user));
    }

    /** Signs in through the real login form and returns the session cookie, like a browser would keep. */
    protected Cookie login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/login")
                .param("email", email)
                .param("password", password)
                .param("cf-turnstile-response", VALID_TURNSTILE_TOKEN)
                .with(csrf()))
            .andExpect(redirectedUrl("/"))
            .andReturn();
        return result.getResponse().getCookie("SESSION");
    }

    protected Duelist createDuelist(UserAuth owner, String name) {
        Duelist duelist = new Duelist(name, "", "Backstory of " + name, "", null);
        duelist.setOwner(owner.getProfile());
        return duelistRepo.save(duelist);
    }

    protected Duel createDuel(Duelist duelist1, Duelist duelist2) {
        return duelRepo.save(new Duel(duelist1, duelist2));
    }

    protected Competition createCompetition(String title, Instant start, int rounds) {
        return competitionRepo.save(new Competition(title, start, rounds));
    }

    protected void enter(Competition competition, Duelist duelist) {
        competitionEntryRepo.save(new CompetitionEntry(competition, duelist));
    }

    /** Writes pending changes and empties the persistence context so later reads come from the database. */
    protected void flushAndClear() {
        em.flush();
        em.clear();
    }

    @BeforeEach
    void configureMock() {
        when(turnstileService.isValidToken(eq(VALID_TURNSTILE_TOKEN), any()))
            .thenReturn(true);

        when(turnstileService.isValidToken(eq(INVALID_TURNSTILE_TOKEN), any()))
            .thenReturn(false);
    }
}
