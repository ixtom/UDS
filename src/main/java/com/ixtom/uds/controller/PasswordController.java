package com.ixtom.uds.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.UserAuthRepository;
import com.ixtom.uds.utils.StringUtils;

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
    public String getChangePassword() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserAuthPrincipal principal,
            @RequestParam(defaultValue = "") String currentPassword,
            @RequestParam(defaultValue = "") String newPassword,
            @RequestParam(defaultValue = "") String confirmPassword,
            Model model, Authentication authentication,
            HttpServletRequest request, HttpServletResponse response) {
        if (principal == null) {
            return "redirect:/login";
        }

        UserAuth user = userAuthRepo.findById(principal.getId()).orElse(null);
        if (user == null || currentPassword.isEmpty()
                || currentPassword.getBytes(StandardCharsets.UTF_8).length > 72
                || !passwordEncoder.matches(currentPassword, user.getHashedPassword())) {
            model.addAttribute("error", "Your current password is incorrect.");
            return "change-password";
        }

        if (!StringUtils.isGoodPassword(newPassword)
                || newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            model.addAttribute("error", "Use at least 8 characters, including uppercase, lowercase, a number and a special character. The maximum is 72 UTF-8 bytes.");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "The new passwords do not match.");
            return "change-password";
        }
        
        if (passwordEncoder.matches(newPassword, user.getHashedPassword())) {
            model.addAttribute("error", "Choose a password different from your current password.");
            return "change-password";
        }

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userAuthRepo.save(user);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?passwordChanged";
    }
}
