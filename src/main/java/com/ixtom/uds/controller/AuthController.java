package com.ixtom.uds.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ixtom.uds.dto.RegisterRequest;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuth.Role;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.repository.UserAuthRepository;
import com.ixtom.uds.repository.UserProfileRepository;
import com.ixtom.uds.service.OAuthHandlerService;
import com.ixtom.uds.service.OAuthHandlerService.OAuthAction;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private static final Duration oauthLinkTimeout = Duration.ofMinutes(15);

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
    private OAuthHandlerService oauthHandlerService;

    // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html

    @GetMapping("/login")
    public String getLogin(@AuthenticationPrincipal UserAuthPrincipal auth) {
        if (auth != null) {
            return "redirect:/";
        }

        return "login";
    }

    @GetMapping("/register")
    public String getRegister(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest(null, null, null));
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest req, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {

        if (userAuthRepo.findByEmail(req.email()).isPresent()) {
            bindingResult.rejectValue("email", "taken", "An account with that email already exists.");
        }

        if (userProfileRepo.findByUsername(req.username()).isPresent()) {
            bindingResult.rejectValue("username", "taken", "An account with that username already exists.");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        UserAuth userAuth = new UserAuth(req.email(), passwordEncoder.encode(req.password()), Role.USER);
        UserProfile profile = new UserProfile(userAuth.getId(), req.username());
        userAuth.setProfile(profile);
        profile.setUserAuth(userAuth);

        userAuthRepo.save(userAuth);
        // userProfileRepo.save(profile);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(userAuth.getEmail(), req.password())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        return "redirect:/";
    }

    @GetMapping("/link/discord")
    public String linkDiscord(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserAuthPrincipal)) {
            return "redirect:/login";
        }

        session.setAttribute("oauthAction", OAuthAction.LINK);
        session.setAttribute("oauthLinkStart", Instant.now());

        return "redirect:/oauth2/authorization/discord"; // oauth stuff will end up redirecting to /login/oauth2/code/{provider}
    }


    @GetMapping("/login/oauth2/code/{provider}")
    public String postOAuthLogin(@PathVariable String provider, HttpSession session, HttpServletResponse response, Model model,
        @RequestParam(required = false) String error, @RequestParam(required = false) String error_description) {

        if (error != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("error", error + " " + error_description);
            return "error/4xx";
        }

        OAuthAction action = (OAuthAction) session.getAttribute("oauthAction");
        String returnPath = (String) session.getAttribute("oauthReturnPath");
        Instant start = (Instant) session.getAttribute("oauthLinkStart");

        session.removeAttribute("oauthAction");
        session.removeAttribute("oauthReturnPath");
        session.removeAttribute("oauthLinkStart");

        if (action == null || (action == OAuthAction.LINK && start == null)) {
            oauthHandlerService.deauthorize(provider);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("error", "Invalid OAuth request");
            return "error/4xx";
        } else if (action == OAuthAction.LINK && timePassed(start, oauthLinkTimeout)) {
            oauthHandlerService.deauthorize(provider);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("error", "Account link request timed out");
            return "error/4xx";
        }

        switch (oauthHandlerService.onOAuthLogin(provider, action)) {
			case LINK_SUCCESS:
				break;
			case NOT_IMPLEMENTED:
			    oauthHandlerService.deauthorize(provider);
				break;
			case OAUTH_ALREADY_LINKED:
			    oauthHandlerService.deauthorize(provider);
				break;
			case OAUTH_CLIENT_MISSING:
				break;
			case USER_ALREADY_LINKED:
                oauthHandlerService.deauthorize(provider);
				break;
			case USER_UNAUTHORIZED:
                oauthHandlerService.deauthorize(provider);
				break;
			default:
				break;
        }

        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        if (returnPath != null) {
            return "redirect:" + returnPath;
        }
        return "redirect:/";
    }

	private static boolean timePassed(Instant start, Duration duration) {
		return !(start.plus(duration).isAfter(Instant.now()));
	}
}
