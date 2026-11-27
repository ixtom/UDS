package com.ixtom.uds.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;

@Entity
@DynamicUpdate
@Table(name="Duelist")
public class Duelist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserProfile owner;

    private String photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portrait_asset_id")
    private ImageAsset portrait;

    @Column(length = 11, nullable = false)
    @Pattern(regexp = "^$|^[a-zA-Z0-9_-]{11}$")
    private String demo;

    @OneToMany(mappedBy = "duelist")
    private List<CompetitionEntry> competitionEntries = new ArrayList<>();

    // methods

    public Duelist() {}

    public Duelist(String name, String title, String description, String demo, String photo) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.demo = demo;
        this.photo = photo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
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

    public UserProfile getOwner() {
        return owner;
    }

    public void setOwner(UserProfile owner) {
        this.owner = owner;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public ImageAsset getPortrait() {
        return portrait;
    }

    public void setPortrait(ImageAsset portrait) {
        this.portrait = portrait;
    }

    public String getPortraitUrl() {
        return portrait == null ? photo : portrait.getUrl();
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

	public List<CompetitionEntry> getCompetitionEntries() {
		return competitionEntries;
	}
}
