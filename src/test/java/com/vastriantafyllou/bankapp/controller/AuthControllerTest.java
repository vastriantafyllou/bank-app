package com.vastriantafyllou.bankapp.controller;

import com.vastriantafyllou.bankapp.core.exception.UsernameAlreadyExistsException;
import com.vastriantafyllou.bankapp.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IUserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Nested
    @DisplayName("GET /login")
    class LoginPageTests {

        @Test
        @DisplayName("should show login page")
        void loginPage() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"));
        }
    }

    @Nested
    @DisplayName("GET /register")
    class RegisterPageTests {

        @Test
        @DisplayName("should show register page")
        void registerPage() throws Exception {
            mockMvc.perform(get("/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attributeExists("registerDTO"));
        }
    }

    @Nested
    @DisplayName("POST /register")
    class RegisterTests {

        @Test
        @DisplayName("should register successfully and redirect to login")
        void register_success() throws Exception {
            mockMvc.perform(post("/register")
                            .param("username", "newuser")
                            .param("password", "password123")
                            .param("confirmPassword", "password123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?registered"));

            verify(userService).register(any());
        }

        @Test
        @DisplayName("should show error when username is blank")
        void register_blankUsername() throws Exception {
            mockMvc.perform(post("/register")
                            .param("username", "")
                            .param("password", "password123")
                            .param("confirmPassword", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attributeHasFieldErrors("registerDTO", "username"));

            verify(userService, never()).register(any());
        }

        @Test
        @DisplayName("should show error when username is too short")
        void register_shortUsername() throws Exception {
            mockMvc.perform(post("/register")
                            .param("username", "ab")
                            .param("password", "password123")
                            .param("confirmPassword", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attributeHasFieldErrors("registerDTO", "username"));
        }

        @Test
        @DisplayName("should show error when password is too short")
        void register_shortPassword() throws Exception {
            mockMvc.perform(post("/register")
                            .param("username", "newuser")
                            .param("password", "12345")
                            .param("confirmPassword", "12345"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attributeHasFieldErrors("registerDTO", "password"));
        }

        @Test
        @DisplayName("should show error when passwords don't match")
        void register_passwordMismatch() throws Exception {
            mockMvc.perform(post("/register")
                            .param("username", "newuser")
                            .param("password", "password123")
                            .param("confirmPassword", "different456"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attributeHasFieldErrors("registerDTO", "confirmPassword"));

            verify(userService, never()).register(any());
        }

        @Test
        @DisplayName("should show error when username already exists")
        void register_usernameExists() throws Exception {
            when(userService.register(any()))
                    .thenThrow(new UsernameAlreadyExistsException("Username taken"));

            mockMvc.perform(post("/register")
                            .param("username", "existinguser")
                            .param("password", "password123")
                            .param("confirmPassword", "password123"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("register"))
                    .andExpect(model().attributeHasFieldErrors("registerDTO", "username"));
        }
    }
}
