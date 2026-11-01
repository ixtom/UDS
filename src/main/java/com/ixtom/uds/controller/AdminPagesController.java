package com.ixtom.uds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ixtom.uds.repository.UserProfileRepository;

@Controller
@RequestMapping("/admin")
public class AdminPagesController {

    @Autowired
    private UserProfileRepository userProfileRepo;


    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("userProfiles", userProfileRepo.findAll());

        return "admin/listusers";
    }
}
