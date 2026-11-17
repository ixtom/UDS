package com.ixtom.uds.controller;

import java.security.Principal;

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

import com.ixtom.uds.dto.EditDuelRequest;
import com.ixtom.uds.dto.NewDuelRequest;
import com.ixtom.uds.dto.VoteRequest;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Vote;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.repository.DuelRepository;
import com.ixtom.uds.repository.UserProfileRepository;
import com.ixtom.uds.repository.VoteRepository;
import com.ixtom.uds.utils.SocialLink;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class DuelController {

    @Autowired
    private DuelRepository duelRepo;

    @Autowired
    private VoteRepository voteRepo;

    @Autowired
    private UserProfileRepository userProfileRepo;
    
    @GetMapping("/upload")
    public String getUpload(Model model) {
        model.addAttribute("newduel", new NewDuelRequest(null, null, null, null, null));

        return "duel/upload";
    }
    @PostMapping("/duelpage/{id}")
    public String submitVote(@PathVariable Long id, @ModelAttribute VoteRequest voteRequest,
        @AuthenticationPrincipal UserAuthPrincipal principal) {
    
    if (principal == null) {
        return "redirect:/login";
    }

    UserProfile voter = userProfileRepo.findById(principal.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    Vote vote = voteRepo.findByDuelIdAndVoterId(id, voter.getId())
            .orElse(new Vote());

    vote.setVoter(voter);
    vote.setDuel(getDuel(id));
    vote.setCat1winner(voteRequest.cat1winner());
    vote.setCat2winner(voteRequest.cat2winner());
    vote.setCat3winner(voteRequest.cat3winner());
    vote.setCat4winner(voteRequest.cat4winner());
    vote.setCat5winner(voteRequest.cat5winner());

    voteRepo.save(vote);
    return "redirect:/duelpage/" + id;
}

    @GetMapping("/duelpage/{id}")
    public String duelPage(@PathVariable Long id, Model model,  @AuthenticationPrincipal UserAuthPrincipal principal) {
        Duel duel = getDuel(id);
        model.addAttribute("duel", duel);

        Vote vote = null;
        if (principal != null) {
            UserProfile voter = userProfileRepo.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            vote = voteRepo.findByDuelIdAndVoterId(id, voter.getId()).orElse(null);
        }

        model.addAttribute("voteRequest", vote == null
        ? new VoteRequest(null, null, null, null, null)
        : new VoteRequest(
                vote.getCat1winner(),
                vote.getCat2winner(),
                vote.getCat3winner(),
                vote.getCat4winner(),
                vote.getCat5winner()));
        Long d1Id = duel.getDuelist1() != null ? duel.getDuelist1().getId() : null;
        Long d2Id = duel.getDuelist2() != null ? duel.getDuelist2().getId() : null;

    model.addAttribute("d1Cat1Votes", d1Id != null ? voteRepo.countByDuelIdAndCat1winner(id, d1Id) : 0);
    model.addAttribute("d2Cat1Votes", d2Id != null ? voteRepo.countByDuelIdAndCat1winner(id, d2Id) : 0);
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

    @GetMapping("/duel/{id}/edit")
    public String getEditDuel(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal, Model model) {
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

        String video = leftSide ? duel.getVideoid1() : duel.getVideoid2();
        if (!video.isEmpty()) {
            video = "youtu.be/" + video;
        }

        model.addAttribute("editDuelRequest",
            new EditDuelRequest(video, duel.getDuelBanner(), duel.getTitle(), duel.getDescription()));

        return "duel/edit";
    }

    @PostMapping("/duel/{id}/edit")
    public String editDuel(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal, @Valid @ModelAttribute EditDuelRequest req, BindingResult bindingResult) {
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

    private Vote getVote(Long duelId, Long voterId) {
        return voteRepo.findByDuelIdAndVoterId(duelId, voterId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
