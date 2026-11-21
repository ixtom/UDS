package com.ixtom.uds.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name = "Matchup")
public class Matchup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;

    @ManyToOne
    @JoinColumn(name = "duelist1_id")
    private Duelist duelist1;

    @ManyToOne
    @JoinColumn(name = "duelist2_id")
    private Duelist duelist2;

    public Matchup() {}

    public Matchup(Competition competition, Duelist duelist1, Duelist duelist2) {
        this.competition = competition;
        this.duelist1 = duelist1;
        this.duelist2 = duelist2;
    }

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

}
