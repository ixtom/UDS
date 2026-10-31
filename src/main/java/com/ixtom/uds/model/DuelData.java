package com.ixtom.uds.model;

import jakarta.persistence.Column;
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
    @Pattern(regexp = "^[a-zA-Z0-9_-]{11}$", message = "Invalid YouTube video ID")
    @Column(name = "videoid1", length = 11, nullable = false)
    private String videoid1;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{11}$", message = "Invalid YouTube video ID")
    @Column(name = "videoid2", length = 11, nullable = false)
    private String videoid2;

    @Column(columnDefinition = "TEXT")
    private String duel_description;

    @Pattern(
        regexp = "^(https?://)?(www\\.|m\\.)?(youtube\\.com/(watch\\?.*v=|embed/|shorts/)|youtu\\.be/)[a-zA-Z0-9_-]{11}.*$",
        message = "Invalid YouTube URL")
    private String rawurl1;
    
    @Pattern(
        regexp = "^(https?://)?(www\\.|m\\.)?(youtube\\.com/(watch\\?.*v=|embed/|shorts/)|youtu\\.be/)[a-zA-Z0-9_-]{11}.*$",
        message = "Invalid YouTube URL")
    private String rawurl2;

    private Long userid1;
    private Long userid2;
    private Long duelistid1;
    private Long duelistid2;
    private int roundnum;
    
    public DuelData() {
    }

    public String getVideoid1() {
        return videoid1;
    }

    public void setVideoid1(String videoid1) {
        this.videoid1 = videoid1;
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

    public String getVideoid2() {
        return videoid2;
    }

    public void setVideoid2(String videoid2) {
        this.videoid2 = videoid2;
    }

    public String getRawurl1() {
        return rawurl1;
    }

    public void setRawurl1(String rawurl1) {
        this.rawurl1 = rawurl1;
    }

    public String getRawurl2() {
        return rawurl2;
    }

    public void setRawurl2(String rawurl2) {
        this.rawurl2 = rawurl2;
    }

    public Long getUserid1() {
        return userid1;
    }

    public void setUserid1(Long userid1) {
        this.userid1 = userid1;
    }

    public Long getUserid2() {
        return userid2;
    }

    public void setUserid2(Long userid2) {
        this.userid2 = userid2;
    }

    public Long getDuelistid1() {
        return duelistid1;
    }

    public void setDuelistid1(Long duelistid1) {
        this.duelistid1 = duelistid1;
    }

    public Long getDuelistid2() {
        return duelistid2;
    }

    public void setDuelistid2(Long duelistid2) {
        this.duelistid2 = duelistid2;
    }

    public int getRoundnum() {
        return roundnum;
    }

    public void setRoundnum(int roundnum) {
        this.roundnum = roundnum;
    }
    
}
