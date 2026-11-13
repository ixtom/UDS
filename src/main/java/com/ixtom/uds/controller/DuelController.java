package com.ixtom.uds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import com.ixtom.uds.dto.NewDuelRequest;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.repository.DuelRepository;
import com.ixtom.uds.utils.SocialLink;

import jakarta.validation.Valid;

@Controller
public class DuelController {

    @Autowired
    private DuelRepository duelRepo;

    @GetMapping("/upload")
    public String getUpload(Model model) {
        model.addAttribute("newduel", new NewDuelRequest(null, null, null, null, null));

        return "duel/upload";
    }

    @GetMapping("/duelpage/{id}")
    public String duelPage(@PathVariable Long id, Model model) {
        Duel duel = duelRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("duel", duel);

        return "duel/view";
    }

    @PostMapping("/upload")
    public String upload(@Valid @ModelAttribute("newduel") NewDuelRequest duelReq, BindingResult bindingResult, Model model) {
        String videoid1 = SocialLink.YOUTUBE_VIDEO.extractId(duelReq.url1());
        String videoid2 = SocialLink.YOUTUBE_VIDEO.extractId(duelReq.url2());

        if (videoid1 == null) { // should not happen
            bindingResult.rejectValue("url1", "invalid", "Could not extract YouTube ID");
        }

        if (videoid2 == null) {
            bindingResult.rejectValue("url2", "invalid", "Could not extract YouTube ID");
        }

        if (bindingResult.hasErrors()) {
            return "duel/upload";
        }

        Duel newDuel = new Duel();

        newDuel.setVideoid1(videoid1);
        newDuel.setVideoid2(videoid2);
        newDuel.setRoundnum(duelReq.round());
        newDuel.setDescription(duelReq.description());
        newDuel.setDuelBanner(duelReq.duelBanner());

        Duel savedDuel = duelRepo.save(newDuel);
        return "redirect:/duelpage/" + savedDuel.getId();
    }
}
