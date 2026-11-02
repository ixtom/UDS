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
    public String getChangePassword() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserAuthPrincipal principal,
            @Valid @ModelAttribute ChangePasswordRequest form, BindingResult bindingResult,
            Model model, Authentication authentication,
            HttpServletRequest request, HttpServletResponse response) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Fill in all three password fields.");
            return "change-password";
        }

        String currentPassword = form.currentPassword();
        String newPassword = form.newPassword();
        String confirmPassword = form.confirmPassword();

        UserAuth user = userAuthRepo.findById(principal.getId()).orElse(null);
        if (user == null || currentPassword.isEmpty()
                || !passwordEncoder.matches(currentPassword, user.getHashedPassword())) {
            model.addAttribute("error", "Your current password is incorrect.");
            return "change-password";
        }

        if (!StringUtils.isGoodPassword(newPassword)) {
            model.addAttribute("error", "Use at least 8 characters, including uppercase, lowercase, a number and a special character.");
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
