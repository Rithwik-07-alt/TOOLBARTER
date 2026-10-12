package com.toolbarter.service;

import com.toolbarter.dto.AuthResponse;
import com.toolbarter.dto.LoginRequest;
import com.toolbarter.dto.RegisterRequest;
import com.toolbarter.dto.UserSummaryResponse;
import com.toolbarter.entity.User;
import com.toolbarter.enums.Role;
import com.toolbarter.enums.TrustLevel;
import com.toolbarter.exception.BadRequestException;
import com.toolbarter.exception.ResourceNotFoundException;
import com.toolbarter.repository.UserRepository;
import com.toolbarter.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("An account with email " + request.getEmail() + " already exists.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone().trim());
        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());
        user.setTrustScore(0);
        user.setTrustLevel(TrustLevel.NEW_NEIGHBOUR);
        user.setCreatedAt(LocalDateTime.now());

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            roles.add(Role.ROLE_OWNER);
            roles.add(Role.ROLE_BORROWER);
        } else {
            roles.addAll(request.getRoles());
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUsername(savedUser.getEmail());
        return new AuthResponse(token, UserSummaryResponse.fromEntity(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String token = tokenProvider.generateToken(authentication);
        return new AuthResponse(token, UserSummaryResponse.fromEntity(user));
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadRequestException("No authenticated user found in session.");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user record not found: " + email));
    }
}
