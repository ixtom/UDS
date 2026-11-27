package com.ixtom.uds.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.service.ImageStorage;
import com.ixtom.uds.service.ImageUpload;
import com.ixtom.uds.service.UserImageService;

@Controller
public class UserImageController {
    private static final Logger log = LoggerFactory.getLogger(UserImageController.class);
    private static final String PROFILE_EDIT = "/account/profile/edit";
    private final UserImageService images;

    public UserImageController(UserImageService images) {
        this.images = images;
    }

    @PostMapping("/account/profile/image")
    public String uploadAvatar(@AuthenticationPrincipal UserAuthPrincipal user,
            @RequestParam(name = "image", required = false) MultipartFile file, RedirectAttributes flash) {
        return editImage(PROFILE_EDIT, flash, "Profile picture saved.", () -> images.uploadAvatar(user.getId(), file));
    }

    @PostMapping("/account/profile/image/remove")
    public String removeAvatar(@AuthenticationPrincipal UserAuthPrincipal user, RedirectAttributes flash) {
        return editImage(PROFILE_EDIT, flash, "Profile picture removed.", () -> images.removeAvatar(user.getId()));
    }

    @PostMapping("/duelist/{id:[0-9]+}/image")
    public String uploadPortrait(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal user,
            @RequestParam(name = "image", required = false) MultipartFile file, RedirectAttributes flash) {
        return editImage("/duelist/" + id + "/edit", flash, "Portrait saved.",
                () -> images.uploadPortrait(user.getId(), id, file));
    }

    @PostMapping("/duelist/{id:[0-9]+}/image/remove")
    public String removePortrait(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal user,
            RedirectAttributes flash) {
        return editImage("/duelist/" + id + "/edit", flash, "Portrait removed.",
                () -> images.removePortrait(user.getId(), id));
    }

    private String editImage(String editPath, RedirectAttributes flash, String successMessage, Runnable action) {
        try {
            action.run();
            flash.addFlashAttribute("imageSuccess", successMessage);
        } catch (ImageUpload.InvalidImage ex) {
            flash.addFlashAttribute("imageError", ex.getMessage());
        } catch (ImageStorage.Failure ex) {
            flash.addFlashAttribute("imageError", "Image storage is unavailable. Try again later.");
        } catch (DataAccessException | TransactionException ex) {
            log.warn("Could not save an image change.", ex);
            flash.addFlashAttribute("imageError", "The image change could not be saved. Try again later.");
        }
        return "redirect:" + editPath;
    }
}
