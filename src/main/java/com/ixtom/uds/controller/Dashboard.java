package com.ixtom.uds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.DuelRepository;

@Controller
@RequestMapping("/dashboard")
public class Dashboard {

    @Autowired
    private DuelRepository duelRepo;

    @GetMapping("")
    public String getOverview() {
        return "dashboard/index";
    }

    @GetMapping("duelists")
    public String getDuelists() {
        return "dashboard/duelists";
    }

    @GetMapping("duels")
    public String getDuels(Model model, @AuthenticationPrincipal UserAuthPrincipal principal) {
        model.addAttribute("matches", duelRepo.findUserDuelsId(principal.getId()));
        return "dashboard/duels";
    }
}
