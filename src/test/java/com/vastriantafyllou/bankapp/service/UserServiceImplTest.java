package com.vastriantafyllou.bankapp.service;

import com.vastriantafyllou.bankapp.core.enums.Role;
import com.vastriantafyllou.bankapp.core.exception.UsernameAlreadyExistsException;
import com.vastriantafyllou.bankapp.dto.RegisterDTO;
import com.vastriantafyllou.bankapp.model.AppUser;
import com.vastriantafyllou.bankapp.repository.AccountRepository;
import com.vastriantafyllou.bankapp.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("should register user successfully")
    void register_success() {
        RegisterDTO dto = new RegisterDTO("newuser", "password123", "password123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AppUser result = userService.register(dto);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getRoles()).contains(Role.USER);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRoles()).contains(Role.USER);
    }

    @Test
    @DisplayName("should throw when username already exists")
    void register_usernameExists() {
        RegisterDTO dto = new RegisterDTO("existinguser", "password123", "password123");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should encode password before saving")
    void register_passwordEncoded() {
        RegisterDTO dto = new RegisterDTO("user1", "rawpass", "rawpass");
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(passwordEncoder.encode("rawpass")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

        AppUser result = userService.register(dto);

        assertThat(result.getPassword()).isEqualTo("$2a$10$encoded");
        verify(passwordEncoder).encode("rawpass");
    }
}
