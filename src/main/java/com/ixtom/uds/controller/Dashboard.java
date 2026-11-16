package com.ixtom.uds.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ixtom.uds.model.CompetitionEntry;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.CompetitionEntryRepository;
import com.ixtom.uds.repository.DuelRepository;

@Controller
@RequestMapping("/dashboard")
public class Dashboard {

    @Autowired
    private DuelRepository duelRepo;

    @Autowired
    private CompetitionEntryRepository competitionEntryRepo;

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
        model.addAttribute("matches", duelRepo.findForUser(principal.getId()));
        return "dashboard/duels";
    }

    @GetMapping("competitions")
    public String getCompetitions(Model model, @AuthenticationPrincipal UserAuthPrincipal principal) {
        List<CompetitionEntry> entries = competitionEntryRepo.findByDuelistOwnerId(principal.getId());
        model.addAttribute("entries", entries);
        return "dashboard/competitions";
    }
}
