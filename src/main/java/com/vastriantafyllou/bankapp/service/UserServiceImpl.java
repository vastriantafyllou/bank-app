package com.vastriantafyllou.bankapp.service;

import com.vastriantafyllou.bankapp.core.enums.Role;
import com.vastriantafyllou.bankapp.core.exception.UserNotFoundException;
import com.vastriantafyllou.bankapp.core.exception.UsernameAlreadyExistsException;
import com.vastriantafyllou.bankapp.core.exception.WrongPasswordException;
import com.vastriantafyllou.bankapp.dto.ChangePasswordDTO;
import com.vastriantafyllou.bankapp.dto.RegisterDTO;
import com.vastriantafyllou.bankapp.dto.UpdateProfileDTO;
import com.vastriantafyllou.bankapp.dto.UserReadOnlyDTO;
import com.vastriantafyllou.bankapp.model.AppUser;
import com.vastriantafyllou.bankapp.repository.AccountRepository;
import com.vastriantafyllou.bankapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AppUser register(RegisterDTO dto) {
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

    @Override
    @Transactional(readOnly = true)
    public AppUser getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Ο χρήστης δεν βρέθηκε"));
    }

    @Override
    @Transactional
    public void updateProfile(String username, UpdateProfileDTO dto) {
        AppUser user = getByUsername(username);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordDTO dto) {
        AppUser user = getByUsername(username);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new WrongPasswordException("Το τρέχον password είναι λάθος");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReadOnlyDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserReadOnlyDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Ο χρήστης δεν βρέθηκε"));
        return mapToUserReadOnlyDTO(user);
    }

    @Override
    @Transactional
    public void blockUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ο χρήστης δεν βρέθηκε"));
        user.setBlocked(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ο χρήστης δεν βρέθηκε"));
        user.setBlocked(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Role role) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ο χρήστης δεν βρέθηκε"));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeRole(Long userId, Role role) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ο χρήστης δεν βρέθηκε"));
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    private UserReadOnlyDTO mapToUserReadOnlyDTO(AppUser user) {
        int accountCount = accountRepository.findByOwner_Username(user.getUsername()).size();
        return new UserReadOnlyDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getBlocked(),
                user.getRoles(),
                accountCount
        );
    }
}
