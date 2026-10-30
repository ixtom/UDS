package com.ixtom.uds.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        newduel.setYturl(convertEmbed(newduel.getYturl()));
        DuelData savedDuel = duelRepo.save(newduel);
        return "redirect:/duelpage/" + savedDuel.getId();
    }

    public static String convertEmbed(String yturl){
    if (yturl.contains("youtu.be/")) {
        String videoId = yturl.substring(yturl.lastIndexOf("/") + 1);
        return "https://www.youtube.com/embed/" + videoId;
    } else if (yturl.contains("watch?v=")) {
        return yturl.replace("watch?v=", "embed/");
    }
        return yturl;
    }
    

}
