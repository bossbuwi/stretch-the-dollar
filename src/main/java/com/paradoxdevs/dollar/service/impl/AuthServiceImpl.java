package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.request.AuthRequest;
import com.paradoxdevs.dollar.api.request.PasswordRequest;
import com.paradoxdevs.dollar.api.response.AuthResponse;
import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.exception.PasswordException;
import com.paradoxdevs.dollar.exception.ErrorCode;
import com.paradoxdevs.dollar.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.AuthMapper;
import com.paradoxdevs.dollar.repository.UserRepository;
import com.paradoxdevs.dollar.service.AuthService;
import com.paradoxdevs.dollar.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Override
    public UserResponse register(AuthRequest request) {
        User user = authMapper.requestToEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.addRole(Role.USER);
        User registeredUser = userRepository.save(user);
        return authMapper.entityToResponseNoId(registeredUser);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public void forgetPassword(AuthRequest request) {

    }

    @Transactional
    @Override
    public void changePassword(PasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(ResourceNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PasswordException();
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new PasswordException(ErrorCode.OLD_NEW_PASSWORD_MATCH);
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new PasswordException(ErrorCode.PASSWORDS_DONT_MATCH);
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
    }
}
