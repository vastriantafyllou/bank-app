package com.vastriantafyllou.bankapp.service;

import com.vastriantafyllou.bankapp.core.exception.UsernameAlreadyExistsException;
import com.vastriantafyllou.bankapp.dto.RegisterDTO;
import com.vastriantafyllou.bankapp.model.AppUser;
import com.vastriantafyllou.bankapp.core.enums.Role;
import com.vastriantafyllou.bankapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AppUser register(RegisterDTO dto) throws UsernameAlreadyExistsException {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameAlreadyExistsException("Το username είναι ήδη σε χρήση");
        }

        AppUser user = AppUser.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();
        user.getRoles().add(Role.USER);

        return userRepository.save(user);
    }
}
