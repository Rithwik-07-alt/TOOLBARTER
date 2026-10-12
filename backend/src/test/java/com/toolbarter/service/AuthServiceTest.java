package com.toolbarter.service;

import com.toolbarter.dto.AuthResponse;
import com.toolbarter.dto.LoginRequest;
import com.toolbarter.dto.RegisterRequest;
import com.toolbarter.entity.User;
import com.toolbarter.enums.Role;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.repository.UserRepository;
import com.toolbarter.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, tokenProvider);
    }

    @Test
    @DisplayName("register hashes password, saves user with dual roles, and returns token")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("plainPass123");
        request.setPhone("1234567890");
        request.setLatitude(17.44);
        request.setLongitude(78.38);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPass123")).thenReturn("$2a$10$hashedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(tokenProvider.generateTokenFromUsername("john@example.com")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("john@example.com", response.getUser().getEmail());
        assertTrue(response.getUser().getRoles().contains(Role.ROLE_OWNER));
        assertTrue(response.getUser().getRoles().contains(Role.ROLE_BORROWER));

        verify(passwordEncoder, times(1)).encode("plainPass123");
    }

    @Test
    @DisplayName("register rejects duplicate email")
    void register_DuplicateEmail_ThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login validates credentials and generates JWT token")
    void login_Success() {
        LoginRequest request = new LoginRequest("john@example.com", "plainPass123");
        Authentication auth = mock(Authentication.class);
        User user = new User();
        user.setId(10L);
        user.setEmail("john@example.com");
        user.setName("John Doe");
        user.setRoles(Set.of(Role.ROLE_BORROWER));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(auth)).thenReturn("valid-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("valid-jwt-token", response.getToken());
        assertEquals("john@example.com", response.getUser().getEmail());
    }
}
