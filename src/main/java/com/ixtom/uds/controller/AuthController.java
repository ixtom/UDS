package com.ixtom.uds.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import com.ixtom.uds.dto.LoginRequest;
import com.ixtom.uds.dto.OAuthRegisterRequest;
import com.ixtom.uds.dto.OAuthUserInfo;
import com.ixtom.uds.dto.RegisterRequest;
import com.ixtom.uds.model.AccountLink;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuth.Role;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.repository.AccountLinkRepository;
import com.ixtom.uds.repository.UserAuthRepository;
import com.ixtom.uds.repository.UserProfileRepository;
import com.ixtom.uds.service.OAuthService;
import com.ixtom.uds.service.OAuthService.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserAuthRepository userAuthRepo;

    @Autowired
    private UserProfileRepository userProfileRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private OAuthService oauthService;

    @Autowired
    private SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @Autowired
    private AccountLinkRepository accountLinkRepo;

    @GetMapping("/login")
    public String getLogin(@AuthenticationPrincipal UserAuthPrincipal auth, Model model) {
        if (auth != null) {
            return "redirect:/";
        }

        model.addAttribute("loginRequest", new LoginRequest(null, null));

        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest req, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(req.email(), req.password()));

            setAuth(authentication, request, response);
            return "redirect:/";
        } catch (AuthenticationException ex) {
            bindingResult.reject("login.failed", "Invalid email or password");
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String getRegister(@AuthenticationPrincipal UserAuthPrincipal auth, Model model) {
        if (auth != null) {
            return "redirect:/";
        }

        model.addAttribute("registerRequest", new RegisterRequest(null, null, null));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest req, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {

        if (userAuthRepo.existsByEmail(req.email())) {
            bindingResult.rejectValue("email", "register.email.already_exists", "An account with this email already exists.");
        }

        if (userProfileRepo.existsByUsername(req.username())) {
            bindingResult.rejectValue("username", "register.username.taken", "This username is already taken.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        createAccount(req.email(), req.username(), req.password());

        Authentication authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(req.email(), req.password())
        );

        setAuth(authentication, request, response);

        return "redirect:/";
    }

    @GetMapping("/link/{provider}")
    public String linkOAuth(@PathVariable String provider, HttpSession session, @AuthenticationPrincipal UserAuthPrincipal principal) {
        validateProviderOrThrow(provider);

        if (principal == null) {
            return "redirect:/login";
        }

        UserAuth user = userAuthRepo.findById(principal.getId()).get();

        if (accountLinkRepo.existsByProviderAndUserId(provider, user.getId())) {
            // TODO: better error messages
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Result.USER_ALREADY_LINKED.message);
        }

        String externalId = oauthService.getExternalId(provider);
        Result result = oauthService.link(provider, externalId, user);

        if (!result.success) {
            oauthService.deauthorize(provider);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, result.message);
        }

        return "redirect:/";
    }

    @GetMapping("/login/{provider}")
    public String loginWithOAuth(@PathVariable String provider, @AuthenticationPrincipal UserAuthPrincipal principal, HttpServletRequest request, HttpServletResponse response) {
        validateProviderOrThrow(provider);

        if (principal != null) { // already logged in
            return "redirect:/";
        }

        OAuthUserInfo oauthUserInfo = oauthService.getOAuthUserInfo(provider); // triggers oauth page
        Optional<AccountLink> accountLink = accountLinkRepo.findByProviderAndExternalId(provider, oauthUserInfo.externalId());

        if (accountLink.isEmpty()) {
            return "redirect:/register/" + provider;
        }

        oauthService.login(accountLink.get(), request, response);

        return "redirect:/";
    }

    @GetMapping("/register/{provider}")
    public String getRegisterWithOAuth(@PathVariable String provider, @AuthenticationPrincipal UserAuthPrincipal principal, Model model, HttpServletRequest request, HttpServletResponse response) {
        validateProviderOrThrow(provider);

        if (principal != null) {
            return "redirect:/";
        }

        OAuthUserInfo oauthUserInfo = oauthService.getOAuthUserInfo(provider);
        Optional<AccountLink> accountLink = accountLinkRepo.findByProviderAndExternalId(provider, oauthUserInfo.externalId());

        if (accountLink.isPresent()) {
            oauthService.login(accountLink.get(), request, response);
            return "redirect:/";
        }

        model.addAttribute("oauthRegisterRequest", new OAuthRegisterRequest(oauthUserInfo.email(), oauthUserInfo.username()));

        return "auth/oauth_register";
    }

    @Transactional
    @PostMapping("/register/{provider}")
    public String registerWithOAuth(@PathVariable String provider, @AuthenticationPrincipal UserAuthPrincipal principal, @Valid @ModelAttribute OAuthRegisterRequest req, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {
        validateProviderOrThrow(provider);

        OAuthUserInfo oauthUserInfo = oauthService.getOAuthUserInfo(provider);
        Optional<AccountLink> accountLink = accountLinkRepo.findByProviderAndExternalId(provider, oauthUserInfo.externalId());

        if (accountLink.isPresent()) {
            oauthService.login(accountLink.get(), request, response);
            return "redirect:/";
        }

        if (userAuthRepo.existsByEmail(req.email())) {
            bindingResult.rejectValue("email", "register.email.already_exists", "An account with this email already exists");
        }
        if (userProfileRepo.existsByUsername(req.username())) {
            bindingResult.rejectValue("username", "register.username.already_taken", "This username unavailable");
        }

        if (bindingResult.hasErrors()) {
            return "auth/oauth_register";
        }

        UserAuth user = createAccount(req.email(), req.username(), null);
        AccountLink link = accountLinkRepo.save(new AccountLink(provider, oauthUserInfo.externalId(), user));
        if (oauthUserInfo.pfpUrl() != null) {
            user.getProfile().setPfp(oauthUserInfo.pfpUrl());
        }

        oauthService.login(link, request, response);

        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        return "redirect:/";
    }

	private UserAuth createAccount(String email, String username, String password) {
	    UserAuth userAuth = new UserAuth(email, passwordEncoder.encode(password), Role.USER);
        UserProfile profile = new UserProfile(userAuth.getId(), username);

        userAuth.setProfile(profile);
        profile.setUserAuth(userAuth);

        return userAuthRepo.save(userAuth);
	}

	private void setAuth(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
	    sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
	}

	private void validateProviderOrThrow(String provider) {
	    if (!oauthService.isProvider(provider)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
	}
}
