package com.TheQuestApp.TheQuestBack.service;


import com.TheQuestApp.TheQuestBack.entity.User;
import com.TheQuestApp.TheQuestBack.repository.UserRepository;
import com.TheQuestApp.TheQuestBack.security.JwtService;
import com.TheQuestApp.TheQuestBack.dto.AuthDTOs.AuthResponse;
import lombok.RequiredArgsConstructor;
import com.TheQuestApp.TheQuestBack.dto.AuthDTOs.UserDTO;
import com.TheQuestApp.TheQuestBack.dto.AuthDTOs.LoginRequest;
import com.TheQuestApp.TheQuestBack.dto.AuthDTOs.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        var token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));

        var token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        var userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getAvatarUrl());
        return new AuthResponse(token, userDTO);
    }
}
