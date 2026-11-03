package com.ixtom.uds.controller;

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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ixtom.uds.dto.RegisterRequest;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.model.UserAuth.Role;
import com.ixtom.uds.repository.UserAuthRepository;
import com.ixtom.uds.repository.UserProfileRepository;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userAuthRepo.findByEmail(req.email()).isPresent()) {
            bindingResult.rejectValue("email", "taken", "An account with that email already exists.");
        }

        if (userProfileRepo.findByUsername(req.username()).isPresent()) {
            bindingResult.rejectValue("username", "taken", "An account with that username already exists.");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        UserAuth userAuth = new UserAuth(req.email(), passwordEncoder.encode(req.password()), null, Role.USER);
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
}
