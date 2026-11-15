package com.ixtom.uds.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import com.ixtom.uds.dto.EditProfileRequest;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.repository.DuelistRepository;
import com.ixtom.uds.repository.UserProfileRepository;

import jakarta.validation.Valid;

@Controller
public class ProfileController {

    private final UserProfileRepository userProfileRepository;
    private final DuelistRepository duelistRepository;

    public ProfileController(UserProfileRepository userProfileRepository, DuelistRepository duelistRepository) {
        this.userProfileRepository = userProfileRepository;
        this.duelistRepository = duelistRepository;
    }

    @GetMapping("/profile/{username}")
    public String viewProfile(@PathVariable String username,
            @AuthenticationPrincipal UserAuthPrincipal currentUser, Model model) {
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("profile", profile);
        model.addAttribute("isOwner", currentUser != null && profile.getId().equals(currentUser.getId()));
        model.addAttribute("duelists", duelistRepository.findByOwnerIdOrderByNameAsc(profile.getId()));

        return "profile/view";
    }

    @GetMapping("/account/profile/edit")
    public String editProfile(@AuthenticationPrincipal UserAuthPrincipal currentUser, Model model) {
        UserProfile profile = ownProfile(currentUser);
        model.addAttribute("editProfile", new EditProfileRequest(
                profile.getBio(), profile.getYoutubeUrl(), profile.getInstagramUrl()));
        return "profile/edit";
    }

    @PostMapping("/account/profile/edit")
    public String saveProfile(@AuthenticationPrincipal UserAuthPrincipal currentUser,
            @Valid @ModelAttribute("editProfile") EditProfileRequest request, BindingResult errors) {
        UserProfile profile = ownProfile(currentUser);

        if (errors.hasErrors()) {
            return "profile/edit";
        }
        profile.setBio(request.bio());
        profile.setYoutubeUrl(request.youtubeUrl());
        profile.setInstagramUrl(request.instagramUrl());
        userProfileRepository.save(profile);
        return "redirect:/profile/" + profile.getUsername();
    }

    private UserProfile ownProfile(UserAuthPrincipal currentUser) {
        return userProfileRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
