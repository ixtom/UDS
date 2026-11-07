package com.ixtom.uds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.UserAuthRepository;

@ControllerAdvice
public class GlobalModelAttributes {

   @Autowired
   private UserAuthRepository userAuthRepository;

    @ModelAttribute("currentUser")
    public UserAuth addUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && (auth.getPrincipal() instanceof UserAuthPrincipal user)) {
            return userAuthRepository.findById(user.getId()).get();
        }

        return null;
    }
}
