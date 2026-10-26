package com.ixtom.uds.service;

import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAuthDetailsService implements UserDetailsService {

    @Autowired
    private UserAuthRepository userAuthReop;

    @Override
    public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException {
        UserAuth authData = userAuthReop
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserAuthPrincipal(authData);
    }
}
