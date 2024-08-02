package com.cardrace.cardrace_server.service;

import com.cardrace.cardrace_server.model.User;
import com.cardrace.cardrace_server.repository.UserRepository;
import com.cardrace.cardrace_server.dto.AuthResponse;
import com.cardrace.cardrace_server.dto.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.cardrace.cardrace_server.exceptions.UsernameTakenException;
import org.springframework.security.authentication.BadCredentialsException;
import com.cardrace.cardrace_server.exceptions.InvalidCredentialsException;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse authenticate(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            String accessToken = jwtService.generateAccessToken(username);
            return new AuthResponse(accessToken);
        } catch (BadCredentialsException e){
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    public AuthResponse registerUser(SignupRequest signupRequest) {
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            throw new UsernameTakenException("Username is already taken");
        }

        User user = new User(
                UUID.randomUUID().toString(),
                signupRequest.getUsername(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getUsername());
        return new AuthResponse(accessToken);
    }
}