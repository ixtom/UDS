package com.ixtom.uds.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.ixtom.uds.dto.EditDuelistRequest;
import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.model.UserProfile;
import com.ixtom.uds.repository.DuelistRepository;
import com.ixtom.uds.repository.UserProfileRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/duelist")
public class DuelistController {

    @Autowired
    private DuelistRepository duelistRepo;

    @Autowired
    private UserProfileRepository userProfileRepo;

    @GetMapping("/{id:[0-9]+}")
    public String viewDuelist(@PathVariable Long id, Model model, @AuthenticationPrincipal UserAuthPrincipal principal) {
        Duelist duelist = getDuelist(id);

        model.addAttribute("duelist", duelist);
        model.addAttribute("isOwner", principal != null && duelist.getOwner().getId().equals(principal.getId()));

        return "duelist/view";
    }

    @GetMapping("/{id:[0-9]+}/edit")
    public String getEditDuelist(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal, Model model, HttpServletResponse response) {
        Duelist duelist = getDuelist(id);

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else if (!duelist.getOwner().getId().equals(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String demo = duelist.getDemo();
        if (!demo.isEmpty()) {
            demo = "youtu.be/" + demo;
        }

        model.addAttribute("editDuelistRequest", new EditDuelistRequest(duelist.getName(), duelist.getTitle(), duelist.getDescription(), demo));
        model.addAttribute("duelistId", id);

        return "duelist/edit";
    }

    @PostMapping("/{id:[0-9]+}/edit")
    public String editDuelist(@PathVariable Long id, @Valid @ModelAttribute EditDuelistRequest req, BindingResult bindingResult, @AuthenticationPrincipal UserAuthPrincipal principal, Model model, HttpServletResponse response) {
        Duelist duelist = getDuelist(id);

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else if (!duelist.getOwner().getId().equals(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String url = req.demoVideo();
        String videoId = url.isEmpty() ? "" : DuelController.getYouTubeId(url);
        if (videoId == null) {
            bindingResult.rejectValue("demoVideo", "invalid", "Could not extract YouTube ID");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("duelistId", id);
            return "duelist/edit";
        }

        duelist.setName(req.name());
        duelist.setTitle(req.title());
        duelist.setDescription(req.description());
        duelist.setDemo(videoId);

        duelistRepo.save(duelist);

        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        return "redirect:/duelist/" + id;
    }

    @GetMapping("/create")
    public String getCreateDuelist(@AuthenticationPrincipal UserAuthPrincipal principal, Model model) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        model.addAttribute("createDuelistRequest", new EditDuelistRequest(null, null, null, null));

        return "duelist/create";
    }

    @PostMapping("/create")
    public String createDuelist(@Valid @ModelAttribute(value = "createDuelistRequest") EditDuelistRequest req, BindingResult bindingResult, @AuthenticationPrincipal UserAuthPrincipal principal, HttpServletResponse response) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // TODO: other validation

        String url = req.demoVideo();
        String videoId = url.isEmpty() ? "" : DuelController.getYouTubeId(url);
        if (videoId == null) {
            bindingResult.rejectValue("demoVideo", "invalid", "Could not extract YouTube ID");
        }

        if (bindingResult.hasErrors()) {
            return "duelist/create";
        }

        UserProfile profile = userProfileRepo.findById(principal.getId()).get();

        Duelist duelist = new Duelist(req.name(), req.title(), req.description(), videoId, null);
        duelist.setOwner(profile);
        duelist = duelistRepo.save(duelist);

        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        return "redirect:/duelist/" + duelist.getId();
    }

    private Duelist getDuelist(long id) {
        return duelistRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
