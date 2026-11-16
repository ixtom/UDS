package com.ixtom.uds.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
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

    @OneToMany(mappedBy = "competition")
    private List<CompetitionEntry> participants = new ArrayList<>();

    public Competition() {}

    public Competition(String title, Instant start, int rounds) {
        this.title = title;
        this.start = start;
        this.rounds = rounds;
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
}
