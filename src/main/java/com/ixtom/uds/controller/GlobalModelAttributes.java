package com.ixtom.uds.controller;

import com.ixtom.uds.model.UserAuthPrincipal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("user")
    public UserAuthPrincipal addUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && (auth.getPrincipal() instanceof UserAuthPrincipal user)) {
            return user;
        }

        return null;
    }
}
