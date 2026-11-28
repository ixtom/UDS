package com.ixtom.uds.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ixtom.uds.dto.EditDuelRequest;
import com.ixtom.uds.dto.NewDuelRequest;
import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.DuelRepository;
import com.ixtom.uds.utils.SocialLink;

import jakarta.validation.Valid;

@Controller
public class DuelController {

    @Autowired
    private DuelRepository duelRepo;

    // @GetMapping("/upload")
    // public String getUpload(Model model) {
    //     model.addAttribute("newduel", new NewDuelRequest(null, null, null, null, null));

    //     return "duel/upload";
    // }

    @GetMapping("/duelpage/{id}")
    public String duelPage(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal, Model model) {
        Duel duel = getDuel(id);

        boolean released = duel.getCompetition() == null || uploadsClosed(duel);
        boolean isOwner = isOwner(duel, principal);
        boolean isAdmin = principal != null && principal.hasRole("ADMIN");

        model.addAttribute("duel", duel);
        model.addAttribute("showVideos", released || isOwner || isAdmin);
        model.addAttribute("canEdit", isOwner && !released);

        return "duel/view";
    }

    // @PostMapping("/upload")
    // public String upload(@Valid @ModelAttribute("newduel") NewDuelRequest duelReq, BindingResult bindingResult, Model model) {
    //     String videoid1 = SocialLink.YOUTUBE_VIDEO.extractId(duelReq.url1());
    //     String videoid2 = SocialLink.YOUTUBE_VIDEO.extractId(duelReq.url2());

    //     if (videoid1 == null) { // should not happen
    //         bindingResult.rejectValue("url1", "invalid", "Could not extract YouTube ID");
    //     }

    //     if (videoid2 == null) {
    //         bindingResult.rejectValue("url2", "invalid", "Could not extract YouTube ID");
    //     }

    //     if (bindingResult.hasErrors()) {
    //         return "duel/upload";
    //     }

    //     Duel newDuel = new Duel();

    //     newDuel.setVideoid1(videoid1);
    //     newDuel.setVideoid2(videoid2);
    //     newDuel.setRoundnum(duelReq.round());
    //     newDuel.setDescription(duelReq.description());
    //     newDuel.setDuelBanner(duelReq.duelBanner());

    //     Duel savedDuel = duelRepo.save(newDuel);
    //     return "redirect:/duelpage/" + savedDuel.getId();
    // }

    @GetMapping("/duel/{id}/edit")
    public String getEditDuel(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal, Model model, RedirectAttributes redirect) {
        Duel duel = getDuel(id);
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        boolean leftSide;
        if (duel.getDuelist1().getOwner().getId().equals(principal.getId())) {
            leftSide = true;
        } else if (duel.getDuelist2().getOwner().getId().equals(principal.getId())) {
            leftSide = false;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (uploadsClosed(duel)) {
            redirect.addFlashAttribute("error", "Uploads for this round are closed.");
            return "redirect:/duelpage/" + id;
        }

        String video = leftSide ? duel.getVideoid1() : duel.getVideoid2();
        if (!video.isEmpty()) {
            video = "youtu.be/" + video;
        }

        model.addAttribute("editDuelRequest",
            new EditDuelRequest(video, duel.getDuelBanner(), duel.getTitle(), duel.getDescription()));

        return "duel/edit";
    }

    @PostMapping("/duel/{id}/edit")
    public String editDuel(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal, @Valid @ModelAttribute EditDuelRequest req, BindingResult bindingResult, RedirectAttributes redirect) {
        Duel duel = getDuel(id);
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        boolean leftSide;
        if (duel.getDuelist1().getOwner().getId().equals(principal.getId())) {
            leftSide = true;
        } else if (duel.getDuelist2().getOwner().getId().equals(principal.getId())) {
            leftSide = false;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        
        if (uploadsClosed(duel)) {
            redirect.addFlashAttribute("error", "Uploads for this round are closed.");
            return "redirect:/duelpage/" + id;
        }

        if (bindingResult.hasErrors()) {
            return "duel/edit";
        }

        String videoId = SocialLink.YOUTUBE_VIDEO.extractId(req.video());
        if (videoId == null) {
            videoId = "";
        }

        if (leftSide) {
            duel.setVideoid1(videoId);
        } else {
            duel.setVideoid2(videoId);
        }

        duel.setDuelBanner(req.banner());
        duel.setTitle(req.title());
        duel.setDescription(req.description());
        duelRepo.save(duel);

        return "redirect:/duelpage/" + id;
    }

    private Duel getDuel(Long id) {
        return duelRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private boolean uploadsClosed(Duel duel) {
        Competition competition = duel.getCompetition();
        if (competition == null || duel.getRoundnum() == null) {
            return false;
        }
        return !Instant.now().isBefore(competition.votingStart(duel.getRoundnum()));
    }

    private boolean isOwner(Duel duel, UserAuthPrincipal principal) {
        if (principal == null) {
            return false;
        }
        Long me = principal.getId();
        return (duel.getUser1() != null && duel.getUser1().getId().equals(me))
            || (duel.getUser2() != null && duel.getUser2().getId().equals(me));
    }
}
