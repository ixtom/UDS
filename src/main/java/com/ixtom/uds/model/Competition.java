package com.ixtom.uds.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Competition")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private Instant start;

    private int rounds;

    @Column(columnDefinition = "integer default 28")
    private int animationDays = 28;

    @Column(columnDefinition = "integer default 7")
    private int votingDays = 7;

    @OneToMany(mappedBy = "competition")
    private List<CompetitionEntry> participants = new ArrayList<>();

    public Competition() {}

    public Competition(String title, Instant start, int rounds) {
        this.title = title;
        this.start = start;
        this.rounds = rounds;
    }

    public Competition(String title, Instant start, int rounds, int animationDays) {
        this.title = title;
        this.start = start;
        this.rounds = rounds;
        this.animationDays = animationDays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public int getAnimationDays() {
        return animationDays;
    }

    public void setAnimationDays(int days) {
        this.animationDays = days;
    }

    public int getVotingDays(){
        return votingDays;
    }

    public void setVotingDays(int days) {
        this.votingDays = days;
    }

    public List<CompetitionEntry> getParticipants() {
        return participants;
    }

    public int getMaxParticipants() {
        // each round, exactly half of the participants get eliminated except for the first one
        return 1 << (rounds - 1);
    }

    public boolean started() {
        return !start.isAfter(Instant.now());
    }

    public int roundDuration() {
        return animationDays + votingDays;
    }

    public Instant roundStart(int round) {
        return start.plus((long) (round - 1) * (animationDays + votingDays), ChronoUnit.DAYS);
    }

    public Instant votingStart(int round) {
        return roundStart(round).plus(animationDays, ChronoUnit.DAYS);
    }

    public Instant votingEnd(int round) {
        return votingStart(round).plus(votingDays, ChronoUnit.DAYS);
    }

    public int competitionDuration() {
        return rounds * (roundDuration());
    }

    public Instant competitionEnd() {
        return votingEnd(rounds);
    }
}
