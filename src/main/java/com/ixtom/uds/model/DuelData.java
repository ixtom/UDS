package com.ixtom.uds.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;

@Entity 
@Table(name="DuelData")
public class DuelData {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Pattern(
        regexp = "^(https?://)?(www\\.)?(youtube\\.com/(watch\\?v=|embed/)|youtu\\.be/)[a-zA-Z0-9_-]+.*$",
        message = "Invalid YouTube URL format"
    )    
    private String yturl;
    private String duel_description;
    
    public DuelData() {
    }

    public String getYturl() {
        return yturl;
    }

    public void setYturl(String yturl) {
        this.yturl = yturl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDuel_description() {
        return duel_description;
    }

    public void setDuel_description(String duel_description) {
        this.duel_description = duel_description;
    }
    
}
