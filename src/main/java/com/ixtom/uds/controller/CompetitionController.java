package com.ixtom.uds.controller;

import java.time.Instant;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ixtom.uds.dto.CreateCompetitionRequest;
import com.ixtom.uds.model.Competition;
import com.ixtom.uds.repository.CompetitionRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;



@Controller 
@RequestMapping("/admin/competitions")
public class CompetitionController {
    
    @Autowired 
    private CompetitionRepository competitionRepo;
    
    @GetMapping("/new")
    public String getCreateCompetition(Model model) {
        model.addAttribute("newcompetition", new CreateCompetitionRequest(null, null));

        return "admin/competition-form";
    }
    
    @PostMapping
    public String postMethodName(
            @Valid @ModelAttribute("newcompetition") CreateCompetitionRequest req, 
            BindingResult bindingResult) {

        Instant start = null;

        if (req.start() != null) {
            start = req.start().atZone(ZoneId.of("America/Vancouver")).toInstant();
            
            if (!start.isAfter(Instant.now())) {
                bindingResult.rejectValue("start", "invalid", "Start must be in the future.");
            }
        }
        if (bindingResult.hasErrors()) {
        return "admin/competition-form";
        }

        competitionRepo.save(new Competition(req.title(), start));

        return "redirect:/admin/competitions";
    }
    
    @GetMapping
    public String listCompetitions(Model model) {
        model.addAttribute("competitions", competitionRepo.findAll());

        return "admin/competitions";
    }


}
