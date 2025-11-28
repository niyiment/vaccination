package com.niyiment.authservice.services;

import com.niyiment.authservice.domain.dto.request.LoginRequest;
import com.niyiment.authservice.domain.dto.request.RefreshTokenRequest;
import com.niyiment.authservice.domain.dto.request.RegisterRequest;
import com.niyiment.authservice.domain.dto.response.AuthResponse;
import com.niyiment.authservice.domain.dto.response.UserResponse;
import com.niyiment.authservice.domain.entity.RefreshToken;
import com.niyiment.authservice.domain.entity.Role;
import com.niyiment.authservice.domain.entity.User;
import com.niyiment.authservice.domain.mapper.UserMapper;
import com.niyiment.authservice.exceptions.AuthServiceException;
import com.niyiment.authservice.repository.RefreshTokenRepository;
import com.niyiment.authservice.repository.RoleRepository;
import com.niyiment.authservice.repository.UserRepository;
import com.niyiment.authservice.services.security.JwtTokenProvider;
import com.niyiment.authservice.services.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * AuthService handling authentication and registration.
 * Manages user authentication, token generation, and user registration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.username());

        try {
            User user = userRepository.findByUsernameOrEmail(request.username())
                    .orElseThrow(() -> new AuthServiceException("Invalid credentials"));
            checkAccountStatus(user);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            handleSuccessfulLogin(user);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return generateAuthResponse(user, userPrincipal);
        } catch (Exception ex) {
            handleFailedLogin(request.username());
            throw ex;
        }
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.username());

        validateUserDoesNotExcist(request);
        User user = createUser(request);
        assignDefaultRole(user);
        User savedUser = userRepository.save(user);

        auditService.logRegisterEvent(savedUser);
        log.info("Registration successful for user: {}", request.username());

        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Refresh token request received");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new AuthServiceException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new AuthServiceException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        AuthResponse authResponse = generateAuthResponse(user, userPrincipal);
        auditService.logTokenRefreshEvent(user);

        return authResponse;
    }

    public void logout(String username) {
        log.info("Logging out user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthServiceException("User not found"));

        refreshTokenRepository.revokeAllUserTokens(user, Instant.now());
        auditService.logLogoutEvent(user);
    }

    @Transactional
    public void revokeAllUserTokens(String username) {
        log.info("Revoking all tokens for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthServiceException("User not found"));

        refreshTokenRepository.revokeAllUserTokens(user, Instant.now());
    }

    private void checkAccountStatus(User user) {
        if (!user.getIsEnabled()) {
            throw new AuthServiceException("User account is disabled");
        }
        if (user.getIsLocked()) {
            throw new AuthServiceException("User account is locked");
        }
    }

    private void handleSuccessfulLogin(User user) {
        user.resetFailedAttempts();
        user.updateLastLogin();
        userRepository.save(user);
        auditService.logSuccessfulLogin(user);
    }

    private void handleFailedLogin(String username) {
        userRepository.findByUsernameOrEmail(username).ifPresent(user -> {
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lock();
                auditService.logAccountLocked(user);
                log.warn("User account locked due to failed login attempts: {}", username);
            }
            userRepository.save(user);
            auditService.logFailedLogin(user);
        });
    }

    private void validateUserDoesNotExcist(RegisterRequest request) {
        if (userRepository.findByUsernameOrEmail(request.username()).isPresent()) {
            throw new AuthServiceException("Username or email is already in use");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new AuthServiceException("Email is already in use");
        }
    }

    private User createUser(RegisterRequest request) {
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.markPasswordChanged();

        return user;
    }

    private void assignDefaultRole(User user) {
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new AuthServiceException("Default role not found"));
        user.addRole(defaultRole);
    }

    private AuthResponse generateAuthResponse(User user, UserPrincipal userPrincipal) {
        String accessToken = jwtTokenProvider.generateAccessToken(user, userPrincipal.getAuthorities());
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);

        Instant expiresAt = jwtTokenProvider.getExpirationFromToken(accessToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getRefreshExpirationMs() /100)
                .expiresAt(expiresAt)
                .user(userMapper.toResponse(user))
                .build();
    }

}
