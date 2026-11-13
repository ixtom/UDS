package com.ixtom.uds.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table (name = "Competition")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@NotBlank 
	private String title;

    private Instant start;

	private int roundDurationDays = 30;
	
    @OneToMany(mappedBy = "competition")
    private List<CompetitionEntry> participants = new ArrayList<>();

	private static final int MAX_LOSSES = 1; // single elimination for now; raise to 2-3 for the loss-based format

    public Competition() {}

	public Competition(String title, Instant start, int roundDurationDays) {
		this.title = title;
		this.start = start;
		this.roundDurationDays = roundDurationDays;
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

	public int getRoundDurationDays() {
		return roundDurationDays;
	}

	public void setRoundDurationDays(int roundDurationDays) {
		this.roundDurationDays = roundDurationDays;
	}

	public List<CompetitionEntry> getParticipants() {
		return participants;
	}

	public int estimateRounds() {
		int n = participants.size();
		if (n < 2) {
			return 0;
		}
		return MAX_LOSSES * (int) Math.ceil(Math.log(n) / Math.log(2));
	}

	
}
