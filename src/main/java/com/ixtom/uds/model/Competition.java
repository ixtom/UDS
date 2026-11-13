package com.ixtom.uds.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
	
	@ManyToMany
	@JoinTable(
		name = "competition_roster",
		joinColumns = @JoinColumn(name = "competition_id"),
		inverseJoinColumns = @JoinColumn(name = "user_profile_id")
	)
	private Set<UserProfile> roster = new HashSet<>();

    @OneToMany(mappedBy = "competition")
    private List<CompetitionEntry> entries = new ArrayList<>();

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

	public Set<UserProfile> getRoster() {
		return roster;
	}

	public void setRoster(Set<UserProfile> roster) {
		this.roster = roster;
	}

	public List<CompetitionEntry> getEntries() {
		return entries;
	}

	
}
