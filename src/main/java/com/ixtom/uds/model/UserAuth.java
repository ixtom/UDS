package com.ixtom.uds.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "UserAuth")
public class UserAuth {

    public enum Role {
        USER,
        // MOD,
        ADMIN,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @OneToOne(
        mappedBy = "userAuth",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private UserProfile profile;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<AccountLink> accountLinks = new ArrayList<>();

    public UserAuth() {}

    public UserAuth(String email, String hashedPassword, Role role) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

	public List<AccountLink> getAccountLinks() {
		return accountLinks;
	}

	public boolean hasRole(String role) {
        return this.role.toString().equals(role);
    }

    public boolean isProviderLinked(String provider) {
        return accountLinks != null && accountLinks.stream()
                .anyMatch(link -> link.getId().getProvider().equals(provider));
    }
}
