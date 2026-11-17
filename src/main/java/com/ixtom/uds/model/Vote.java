package com.ixtom.uds.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity 
@Table(
    name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"duel_id", "voter_id"})
)
public class Vote {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duel_id", nullable = false)
    private Duel duel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private UserProfile voter;

    private Long cat1winner;
    private Long cat2winner;
    private Long cat3winner;
    private Long cat4winner;
    private Long cat5winner;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Duel getDuel() {
        return duel;
    }
    public void setDuel(Duel duel) {
        this.duel = duel;
    }
    public UserProfile getVoter() {
        return voter;
    }
    public void setVoter(UserProfile voter) {
        this.voter = voter;
    }
    public Long getCat1winner() {
        return cat1winner;
    }
    public void setCat1winner(Long cat1winner) {
        this.cat1winner = cat1winner;
    }
    public Long getCat2winner() {
        return cat2winner;
    }
    public void setCat2winner(Long cat2winner) {
        this.cat2winner = cat2winner;
    }
    public Long getCat3winner() {
        return cat3winner;
    }
    public void setCat3winner(Long cat3winner) {
        this.cat3winner = cat3winner;
    }
    public Long getCat4winner() {
        return cat4winner;
    }
    public void setCat4winner(Long cat4winner) {
        this.cat4winner = cat4winner;
    }
    public Long getCat5winner() {
        return cat5winner;
    }
    public void setCat5winner(Long cat5winner) {
        this.cat5winner = cat5winner;
    }
    
}
