package com.ixtom.uds.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Audited.Table;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "Competition")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant start;

    @Min(1)
    private int rounds;

    @OneToMany(mappedBy = "competition")
    private List<CompetitionEntry> participants = new ArrayList<>();

    public Competition() {}

    public Competition(Instant start, Integer rounds) {
        this.start = start;
        this.rounds = rounds;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public int getMaxParticipants() {
	    return 1 << rounds; // 2 ^ rounds
	}
}
