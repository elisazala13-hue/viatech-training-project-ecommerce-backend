package com.ecommerce.services;

import com.ecommerce.models.DTO.requests.LoginRequest;
import com.ecommerce.models.DTO.requests.RefreshRequest;
import com.ecommerce.models.DTO.responses.LoginResponse;
import com.ecommerce.models.DTO.requests.RegisterRequest;
import com.ecommerce.models.DTO.responses.UserResponse;
import com.ecommerce.exceptions.EmailAlreadyExistsException;
import com.ecommerce.models.entities.User;
import com.ecommerce.repositories.UserRepository;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;


    @Transactional
    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName().trim());

        return UserResponse.from(userRepository.save(user));
    }

    public LoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.email().trim().toLowerCase(), req.password()));

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return tokensFor(principal);
    }

    public LoginResponse refresh(RefreshRequest req) {
        try {
            String email = jwtService.extractUsernameFromRefresh(req.refreshToken());
            CustomUserDetails principal =
                    (CustomUserDetails) userDetailsService.loadUserByUsername(email);
            if (!principal.isEnabled()) {
                throw new DisabledException("User is not active");
            }
            return tokensFor(principal);
        } catch (JwtException | UsernameNotFoundException | DisabledException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
    }

    private LoginResponse tokensFor(CustomUserDetails principal) {
        return new LoginResponse(
                jwtService.generateAccessToken(principal),
                jwtService.generateRefreshToken(principal),
                "Bearer",
                UserResponse.from(principal.getUser()));
    }
}