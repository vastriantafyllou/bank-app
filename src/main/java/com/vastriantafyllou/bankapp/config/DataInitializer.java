package com.vastriantafyllou.bankapp.config;

import com.vastriantafyllou.bankapp.model.AppUser;
import com.vastriantafyllou.bankapp.core.enums.Role;
import com.vastriantafyllou.bankapp.repository.AccountRepository;
import com.vastriantafyllou.bankapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.username:admin}")
    private String adminUsername;

    @Value("${app.security.admin.password:admin}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            AppUser admin = userRepository.findByUsername(adminUsername)
                    .orElseGet(() -> {
                        AppUser u = AppUser.builder()
                                .username(adminUsername)
                                .password(passwordEncoder.encode(adminPassword))
                                .build();
                        u.getRoles().add(Role.ADMIN);
                        u.getRoles().add(Role.USER);
                        return userRepository.save(u);
                    });

            var orphanAccounts = accountRepository.findAllByOwnerIsNull();
            for (var account : orphanAccounts) {
                account.setOwner(admin);
            }
            if (!orphanAccounts.isEmpty()) {
                accountRepository.saveAll(orphanAccounts);
            }
        };
    }
}
