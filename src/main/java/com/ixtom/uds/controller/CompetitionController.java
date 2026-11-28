package com.ixtom.uds.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ixtom.uds.dto.CreateCompetitionRequest;
import com.ixtom.uds.dto.JoinCompetitionRequest;
import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.CompetitionEntry;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.Matchup;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.CompetitionEntryRepository;
import com.ixtom.uds.repository.CompetitionRepository;
import com.ixtom.uds.repository.DuelRepository;
import com.ixtom.uds.repository.DuelistRepository;
import com.ixtom.uds.repository.MatchupRepository;
import com.ixtom.uds.service.DuelScoringService;

import jakarta.validation.Valid;

@Controller
public class CompetitionController {

    @Autowired
    private CompetitionRepository competitionRepo;

    @Autowired
    private DuelistRepository duelistRepo;

    @Autowired
    private CompetitionEntryRepository competitionEntryRepo;

    @Autowired 
    private DuelRepository duelRepo;

    @Autowired 
    private MatchupRepository matchupRepo;

    @Autowired 
    private DuelScoringService duelScoringService;

    @GetMapping("/admin/competitions/new")
    public String getCreateCompetition(Model model) {
        model.addAttribute("createCompetitionRequest", new CreateCompetitionRequest(null, null, 5));

        return "competition/form";
    }

    @PostMapping("/admin/competitions/new")
    public String createCompetition(
            @Valid @ModelAttribute CreateCompetitionRequest req,
            BindingResult bindingResult) {

        Instant start = req.start();

        if (start != null && !start.isAfter(Instant.now())) {
            bindingResult.rejectValue("start", "invalid", "Start must be in the future.");
        }
        if (bindingResult.hasErrors()) {
            return "competition/form";
        }

        Competition saved = competitionRepo.save(new Competition(req.title(), start, req.rounds()));

        return "redirect:/admin/competitions/" + saved.getId();
    }

    @GetMapping("/competitions")
    public String listCompetitions(Model model) {
        model.addAttribute("competitions", competitionRepo.findAll());

        return "competition/list";
    }

    @GetMapping("/competitions/{id}")
    public String viewCompetition(@PathVariable Long id, Model model, @AuthenticationPrincipal UserAuthPrincipal principal) {
        Competition competition = getCompetitionOrThrow(id);

        if (principal != null) {
            boolean canJoin = canJoinCompetition(competition, principal.getId());
            boolean canLeave = !canJoin && canLeaveCompetition(competition, principal.getId());
            model.addAttribute("canJoin", canJoin);
            model.addAttribute("canLeave", canLeave);
        }

        List<Duel> duels = duelRepo.findByCompetitionIdOrderByRoundnum(id);
        for(Duel duel : duels){
            duelScoringService.finalizeDuelIfEnded(duel);
        }

        model.addAttribute("competition", competition);
        model.addAttribute("duels", duelRepo.findByCompetitionIdOrderByRoundnum(id));
        model.addAttribute("leaderboard", competitionEntryRepo.findByCompetitionIdOrderByScoreDescDuelistNameAsc(id));
        return "competition/detail";
    }

    @GetMapping("/admin/competitions/{id}")
    public String getCompetitionInfo(@PathVariable Long id, Model model) {

        Competition competition = getCompetitionOrThrow(id);
        model.addAttribute("competition", competition);
        model.addAttribute("duels", duelRepo.findByCompetitionIdOrderByRoundnum(id));
        model.addAttribute("matchups", matchupRepo.findByCompetitionId(id));

        return "competition/manage";
    }

    @GetMapping("/competitions/{id}/join")
    public String getJoinCompetition(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        } else if (!canJoinCompetition(getCompetitionOrThrow(id), principal.getId())) {
            return "redirect:/competitions/" + id;
        }

        model.addAttribute("duelists", duelistRepo.findByOwnerIdOrderByNameAsc(principal.getId()));
        model.addAttribute("joinCompetitionRequest", new JoinCompetitionRequest(null));

        return "competition/join";
    }

    @PostMapping("/competitions/{id}/join")
    public String joinCompetition(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal, @Valid @ModelAttribute JoinCompetitionRequest req, BindingResult bindingResult, Model model) {
        Competition competition = getCompetitionOrThrow(id);

        if (principal == null) {
            return "redirect:/login";
        } else if (!canJoinCompetition(competition, principal.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cant enter this competition");
        }

        // both of these cases generally should not happen but are possible
        Duelist duelist = null;
        if (req.duelistId() != null) {
            duelist = duelistRepo.findById(req.duelistId()).orElse(null);
            if (duelist == null) {
                bindingResult.rejectValue("duelistId", "invalid", "Duelist does not exist");
            } else if (!duelist.getOwner().getId().equals(principal.getId())) {
                bindingResult.rejectValue("duelistId", "invalid", "You dont own this duelist");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("duelists", duelistRepo.findByOwnerIdOrderByNameAsc(principal.getId()));
            return "competition/join";
        }

        competitionEntryRepo.save(new CompetitionEntry(competition, duelist));

        return "redirect:/competitions/" + id;
    }

    @PostMapping("/competitions/{id}/leave")
    public String leaveCompetition(@PathVariable Long id, @AuthenticationPrincipal UserAuthPrincipal principal) {
        Competition competition = getCompetitionOrThrow(id);

        if (principal == null) {
            return "redirect:/login";
        }

        if (!canLeaveCompetition(competition, principal.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Can not leave competition");
        }

        competitionEntryRepo.deleteByCompetitionIdAndDuelistOwnerId(competition.getId(), principal.getId());

        return "redirect:/competitions/" + id;
    }

    private Competition getCompetitionOrThrow(Long id) {
        return competitionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private boolean canJoinCompetition(Competition competition, Long userId) {
        return !competition.started()
            && competitionEntryRepo.countByCompetitionId(competition.getId()) < competition.getMaxParticipants()
            && !competitionEntryRepo.existsByCompetitionIdAndDuelistOwnerId(competition.getId(), userId)
            && duelistRepo.existsByOwnerId(userId);
    }

    private boolean canLeaveCompetition(Competition competition, Long userId) {
        return !competition.started()
            && competitionEntryRepo.existsByCompetitionIdAndDuelistOwnerId(competition.getId(), userId);
    }

    @PostMapping("/admin/competitions/{id}/matchups")
    public String generateMatchups(@PathVariable Long id, RedirectAttributes redirect) {
        Competition competition = getCompetitionOrThrow(id);
        int round = duelRepo.findLatestRound(id) + 1;
        if (!competition.started()) {
            redirect.addFlashAttribute("error", "The competition hasn't started yet.");
            return "redirect:/admin/competitions/" + id;
        }
        if (round > competition.getRounds()) {
            redirect.addFlashAttribute("error", "All rounds are already paired.");
            return "redirect:/admin/competitions/" + id;
        }

        matchupRepo.deleteByCompetitionId(id);

        List<Duelist> pool = new ArrayList<>();
        for (CompetitionEntry entry : competition.getParticipants()) {
            if (!entry.isEliminated()) {
                pool.add(entry.getDuelist());
            }
        }
        Collections.shuffle(pool);

        while (pool.size() >= 2) {
            Duelist a = pool.remove(0);

            Duelist opponent = null;
            for (Duelist b : pool) {
                if (!duelRepo.existsMatchInCompetition(id, a.getId(), b.getId())) {
                    opponent = b;
                    break;
                }
            }
            if (opponent == null) {
                opponent = pool.get(0);
            }
            pool.remove(opponent);

            matchupRepo.save(new Matchup(competition, a, opponent));
        }

        return "redirect:/admin/competitions/" + id;
    }

    @Transactional 
    @PostMapping("/admin/competitions/{id}/duels")
    public String createDuels(@PathVariable Long id, RedirectAttributes redirect) {
        Competition competition = getCompetitionOrThrow(id);
        int round = duelRepo.findLatestRound(id) + 1;
        if (!competition.started()) {
            redirect.addFlashAttribute("error", "The competition hasn't started yet.");
            return "redirect:/admin/competitions/" + id;
        }
        if (round > competition.getRounds()) {
            redirect.addFlashAttribute("error", "All rounds are already paired.");
            return "redirect:/admin/competitions/" + id;
        }

        List<Matchup> matchups = matchupRepo.findByCompetitionId(id);
        if (matchups.isEmpty()) {
            redirect.addFlashAttribute("error", "No draft matchups. Generate matchups first.");
            return "redirect:/admin/competitions/" + id;
        }

        for (Matchup matchup : matchups) {
            Duel duel = new Duel(competition, round, matchup.getDuelist1(), matchup.getDuelist2());
            Instant now = Instant.now();
            duel.setStartDate(now);
            duel.setEndDate(now.plus(30, ChronoUnit.DAYS));
            duelRepo.save(duel);
        }
        matchupRepo.deleteByCompetitionId(id);

        return "redirect:/admin/competitions/" + id;
    }
    
    
}
