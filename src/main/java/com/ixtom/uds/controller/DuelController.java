package com.ixtom.uds.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.ixtom.uds.model.DuelData;
import com.ixtom.uds.repository.DuelRepository;

import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Controller 
public class DuelController {
    @Autowired 
    private DuelRepository duelRepo;

    @GetMapping("/upload")
    public String uploadVideo(Model model){
        model.addAttribute("newduel", new DuelData());
        return "upload";
    }

    @GetMapping("/duelpage/{id}")
    public String duelPage(@PathVariable Long id, Model model){
        DuelData duel = duelRepo.findById(id).orElseThrow();
        model.addAttribute("newduel", duel);
        return "duelpage";
    }

    @PostMapping("/upload")
    public String upload(@Valid @ModelAttribute("newduel") DuelData newduel, BindingResult binding_result, Model model){
        if(binding_result.hasErrors()){
            return "upload";
        }

        String videoid1 = getYouTubeId(newduel.getRawurl1());
        String videoid2 = getYouTubeId(newduel.getRawurl2());
        if(videoid1 == null){
            binding_result.rejectValue("rawurl1", "invalid", "Could not extract YouTube ID");
        }

        if(videoid2 == null){
            binding_result.rejectValue("rawurl2", "invalid", "Could not extract YouTube ID");

        }

        newduel.setVideoid1(videoid1);
        newduel.setVideoid2(videoid2);
        DuelData savedDuel = duelRepo.save(newduel);
        return "redirect:/duelpage/" + savedDuel.getId();
    }

    public String getYouTubeId (String youTubeUrl) {
        if (youTubeUrl == null || youTubeUrl.isBlank()) {
        return null;
        }
        String pattern = "(?:youtu\\.be/|embed/|videos/|shorts/|(?:watch\\?|.*[&?])v=)([^#&?\\s]{11})";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null;

    }
    

}
