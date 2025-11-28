package com.niyiment.authservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niyiment.authservice.domain.dto.request.LoginRequest;
import com.niyiment.authservice.domain.dto.request.RegisterRequest;
import com.niyiment.authservice.domain.entity.Role;
import com.niyiment.authservice.domain.entity.User;
import com.niyiment.authservice.repository.RoleRepository;
import com.niyiment.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clean up
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create default role
        userRole = Role.builder()
                .name("ROLE_USER")
                .description("Standard user role")
                .permissions(new HashSet<>())
                .build();
        userRole = roleRepository.save(userRole);
    }

    @Test
    void register_WithValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void register_WithInvalidPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("weak")
                .firstName("John")
                .lastName("Doe")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void register_WithDuplicateUsername_ShouldReturnBadRequest() throws Exception {
        // Arrange
        User existingUser = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName("Existing")
                .lastName("User")
                .isEnabled(true)
                .isLocked(false)
                .roles(new HashSet<>())
                .build();
        existingUser.addRole(userRole);
        userRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("different@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username or email is already in use"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // Arrange
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName("Test")
                .lastName("User")
                .isEnabled(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .roles(new HashSet<>())
                .build();
        user.addRole(userRole);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("testuser", "Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange: create a real user, then attempt login with wrong password so that
        // Spring Security raises AuthenticationException -> 401 from GlobalExceptionHandler
        User user = User.builder()
                .username("badloginuser")
                .email("badlogin@example.com")
                .passwordHash(passwordEncoder.encode("CorrectPass1!"))
                .isEnabled(true)
                .isLocked(false)
                .roles(new HashSet<>())
                .build();
        user.addRole(userRole);
        userRepository.save(user);

        LoginRequest request = new LoginRequest("badloginuser", "WrongPass1!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication failed: Invalid credentials"));
    }

    @Test
    void login_WithLockedAccount_ShouldReturnBadRequest() throws Exception {
        // Arrange
        User lockedUser = User.builder()
                .username("lockeduser")
                .email("locked@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName("Locked")
                .lastName("User")
                .isEnabled(true)
                .isLocked(true)
                .failedLoginAttempts(5)
                .roles(new HashSet<>())
                .build();
        lockedUser.addRole(userRole);
        userRepository.save(lockedUser);

        LoginRequest request = new LoginRequest("lockeduser", "Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User account is locked"));
    }
}
