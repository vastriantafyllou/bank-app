package com.vastriantafyllou.bankapp.service;

import com.vastriantafyllou.bankapp.model.AppUser;
import com.vastriantafyllou.bankapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (Boolean.TRUE.equals(appUser.getBlocked())) {
            throw new DisabledException("Ο λογαριασμός σας έχει αποκλειστεί. Επικοινωνήστε με τον διαχειριστή.");
        }

        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())
                .authorities(appUser.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                        .collect(Collectors.toSet()))
                .build();
    }
}
