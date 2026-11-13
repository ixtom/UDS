package com.ixtom.uds.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.ixtom.uds.dto.CreateCompetitionRequest;
import com.ixtom.uds.model.Competition;
import com.ixtom.uds.repository.CompetitionRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestBody;





@Controller 
public class CompetitionController {
    
    @Autowired 
    private CompetitionRepository competitionRepo;
    
    @GetMapping("/admin/competitions/new")
    public String getCreateCompetition(Model model) {
        model.addAttribute("newcompetition", new CreateCompetitionRequest(null, null));

        return "competition/form";
    }
    
    @PostMapping("/admin/competitions")
    public String createCompetition(
            @Valid @ModelAttribute("newcompetition") CreateCompetitionRequest req, 
            BindingResult bindingResult) {

        Instant start = req.start();

        if (start != null && !start.isAfter(Instant.now())) {
            bindingResult.rejectValue("start", "invalid", "Start must be in the future.");
        } if (bindingResult.hasErrors()) {
        return "competition/form";
        }

        Competition saved = competitionRepo.save(new Competition(req.title(), start));

        return "redirect:/admin/competitions/" + saved.getId();
    }
    
    @GetMapping("/competitions")
    public String listCompetitions(Model model) {
        model.addAttribute("competitions", competitionRepo.findAll());

        return "competition/list";
    }

    @GetMapping("/competitions/{id}")
    public String viewCompetition(@PathVariable Long id, Model model) {
        Competition competition = competitionRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("competition", competition);

        return "competition/detail";
    }

    @GetMapping("/admin/competitions/{id}")
    public String getCompetitionInfo(@PathVariable Long id, Model model) { 
        Competition competition = competitionRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("competition", competition);

        return "competition/manage";
    }
}
