package com.sprintflow.backend.service;

import com.sprintflow.backend.dto.auth.AuthResponse;
import com.sprintflow.backend.dto.auth.LoginRequest;
import com.sprintflow.backend.dto.auth.RegisterRequest;
import com.sprintflow.backend.entity.User;
import com.sprintflow.backend.repository.UserRepository;
import com.sprintflow.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request){

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email already Registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }

    public AuthResponse login (LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->
                        new RuntimeException("Invalid Email or password"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities(Collections.emptyList())
                        .disabled(!user.isEnabled())
                        .build()
        );

        AuthResponse response = new AuthResponse();
        response.setToken(token);

        return response;
    }
}
