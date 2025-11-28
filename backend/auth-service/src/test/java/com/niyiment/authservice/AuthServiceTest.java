package com.niyiment.authservice;

import com.niyiment.authservice.domain.dto.request.LoginRequest;
import com.niyiment.authservice.domain.dto.request.RegisterRequest;
import com.niyiment.authservice.domain.dto.response.AuthResponse;
import com.niyiment.authservice.domain.dto.response.UserResponse;
import com.niyiment.authservice.domain.entity.Role;
import com.niyiment.authservice.domain.entity.User;
import com.niyiment.authservice.domain.mapper.UserMapper;
import com.niyiment.authservice.exceptions.AuthServiceException;
import com.niyiment.authservice.exceptions.UserAlreadyExistsException;
import com.niyiment.authservice.repository.RefreshTokenRepository;
import com.niyiment.authservice.repository.RoleRepository;
import com.niyiment.authservice.repository.UserRepository;
import com.niyiment.authservice.services.AuditService;
import com.niyiment.authservice.services.AuthService;
import com.niyiment.authservice.services.security.JwtTokenProvider;
import com.niyiment.authservice.services.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .permissions(new HashSet<>())
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("Password123!")
                .isEnabled(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .roles(new HashSet<>())
                .build();
        testUser.addRole(testRole);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "Password123!");
        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(tokenProvider.generateAccessToken(any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(tokenProvider.getRefreshExpirationMs()).thenReturn(86400000L);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(userRepository).save(testUser);
        verify(auditService).logSuccessfulLogin(testUser);
        assertEquals(0, testUser.getFailedLoginAttempts());
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthServiceException.class, () -> authService.login(request));
    }

    @Test
    void login_WithLockedAccount_ShouldThrowException() {
        // Arrange
        testUser.lock();
        LoginRequest request = new LoginRequest("testuser", "password123");

        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AuthServiceException.class, () -> authService.login(request));
    }

    @Test
    void register_WithValidRequest_ShouldCreateUser() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        User newUser = User.builder()
                .username(request.username())
                .email(request.email())
                .build();

        UserResponse expectedResponse = userMapper.toResponse(newUser);

        // Align stubs with AuthService implementation which uses findByUsernameOrEmail/findByEmail
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userMapper.toEntity(any())).thenReturn(newUser);
        when(passwordEncoder.encode(anyString())).thenReturn("Password123!");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any())).thenReturn(newUser);
        when(userMapper.toResponse(any())).thenReturn(expectedResponse);

        // Act
        UserResponse response = authService.register(request);

        // Assert
//        assertNotNull(response);
        verify(userRepository).save(any(User.class));
        verify(auditService).logRegisterEvent(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        // AuthService checks by username OR email first
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AuthServiceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("existing@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        // First check returns empty, then email check returns present
        when(userRepository.findByUsernameOrEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(AuthServiceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void logout_WithValidUser_ShouldRevokeTokens() {
        // Arrange
        String username = "testuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        authService.logout(username);

        // Assert
        verify(refreshTokenRepository).revokeAllUserTokens(eq(testUser), any());
        verify(auditService).logLogoutEvent(testUser);
    }
}

