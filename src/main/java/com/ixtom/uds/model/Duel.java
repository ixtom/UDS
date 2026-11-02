package com.ixtom.uds.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name="DuelData")
public class Duel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;
    private Integer roundnum;

    // change in later iteration
    private String duelBanner;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "user1_id")
    private UserProfile user1;

    @ManyToOne
    @JoinColumn(name = "user2_id")
    private UserProfile user2;

    @ManyToOne
    @JoinColumn(name = "duelist1_id")
    private Duelist duelist1;

    @ManyToOne
    @JoinColumn(name = "duelist2_id")
    private Duelist duelist2;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{11}$", message = "Invalid YouTube video ID")
    @Column(name = "videoid1", length = 11)
    private String videoid1;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{11}$", message = "Invalid YouTube video ID")
    @Column(name = "videoid2", length = 11)
    private String videoid2;

    public Duel() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public Integer getRoundnum() {
        return roundnum;
    }

    public void setRoundnum(Integer roundnum) {
        this.roundnum = roundnum;
    }

    public String getDuelBanner() {
        return duelBanner;
    }

    public void setDuelBanner(String duelBanner) {
        this.duelBanner = duelBanner;
    }

    public String getTitle() {
        return title;
    }

    public String getDefaultTitle() {
        if (title != null && !title.isBlank()) {
            return title;
        }
        String matchup = nameOf(duelist1) + " vs " + nameOf(duelist2);
        if (roundnum != null) {
            return "Round " + roundnum + ": " + matchup;
        }
        return matchup;
    }

    private String nameOf(Duelist d) {
        return (d != null && d.getName() != null) ? d.getName() : "TBD";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserProfile getUser1() {
        return user1;
    }

    public void setUser1(UserProfile user1) {
        this.user1 = user1;
    }

    public UserProfile getUser2() {
        return user2;
    }

    public void setUser2(UserProfile user2) {
        this.user2 = user2;
    }

    public Duelist getDuelist1() {
        return duelist1;
    }

    public void setDuelist1(Duelist duelist1) {
        this.duelist1 = duelist1;
    }

    public Duelist getDuelist2() {
        return duelist2;
    }

    public void setDuelist2(Duelist duelist2) {
        this.duelist2 = duelist2;
    }

    public String getVideoid1() {
        return videoid1;
    }

    public void setVideoid1(String videoid1) {
        this.videoid1 = videoid1;
    }

    public String getVideoid2() {
        return videoid2;
    }

    public void setVideoid2(String videoid2) {
        this.videoid2 = videoid2;
    }
}
