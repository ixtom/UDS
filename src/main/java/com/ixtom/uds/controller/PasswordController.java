package com.ixtom.uds.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ixtom.uds.dto.ChangePasswordRequest;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.UserAuthRepository;
import com.ixtom.uds.utils.StringUtils;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class PasswordController {
    private final UserAuthRepository userAuthRepo;
    private final PasswordEncoder passwordEncoder;

    public PasswordController(UserAuthRepository userAuthRepo, PasswordEncoder passwordEncoder) {
        this.userAuthRepo = userAuthRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/change-password")
    public String getChangePassword(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest(null, null, null));
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserAuthPrincipal principal,
            @Valid @ModelAttribute ChangePasswordRequest req, BindingResult bindingResult,
            Model model, Authentication authentication,
            HttpServletRequest request, HttpServletResponse response) {
        if (principal == null) {
            return "redirect:/login";
        }

        String newPassword = req.newPassword();

        UserAuth user = userAuthRepo.findById(principal.getId()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.currentPassword(), user.getHashedPassword())) {
            bindingResult.rejectValue("currentPassword", "wrongpassword", "Incorrect password");
        }

        if (!newPassword.equals(req.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismachingpasswords", "Passwords do not match");
        }

        if (passwordEncoder.matches(newPassword, user.getHashedPassword())) {
            model.addAttribute("error", "Choose a password different from your current password.");
            bindingResult.rejectValue("newPassword", "samepassworderror", "Password must not be your current password");
        }

        if (bindingResult.hasErrors()) {
            return "change-password";
        }

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userAuthRepo.save(user);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?passwordChanged";
    }
}
