package com.ixtom.uds.model;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserAuthPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String hashedPassword;
    private Collection<GrantedAuthority> authorities;

    public UserAuthPrincipal(UserAuth authData) {
        this.id = authData.getId();
        this.username = authData.getProfile().getUsername();
        this.email = authData.getEmail();
        this.hashedPassword = authData.getHashedPassword();
        this.authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + authData.getRole().name())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return hashedPassword;
    }

    @Override
    public String getUsername() { // login identifier, not username
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
}
