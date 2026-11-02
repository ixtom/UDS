package com.ixtom.uds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ixtom.uds.repository.DuelRepository;

@Controller
public class MiscController {

    @Autowired
    private DuelRepository duelRepo;

    @GetMapping("/")
    public String getIndex(Model model) {
        model.addAttribute("duels", duelRepo.findAll());

        return "index";
    }
}
