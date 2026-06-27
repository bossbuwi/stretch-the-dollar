package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.request.AuthRequest;
import com.paradoxdevs.dollar.api.response.AuthResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.mapper.AuthMapper;
import com.paradoxdevs.dollar.model.UserDto;
import com.paradoxdevs.dollar.repository.UserRepository;
import com.paradoxdevs.dollar.service.AuthService;
import com.paradoxdevs.dollar.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthMapper authMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
    }

    @Override
    public AuthResponse register(AuthRequest request) {
        User user = authMapper.requestToEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.addRole(Role.USER);
        User registeredUser = userRepository.save(user);
        return AuthResponse.builder()
                .username(registeredUser.getUsername())
                .build();
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        UserDto userDto = authMapper.requestToDto(request);
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword())
        );
        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }
}
