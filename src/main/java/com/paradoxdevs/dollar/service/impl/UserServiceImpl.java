package com.paradoxdevs.dollar.service.impl;

import com.paradoxdevs.dollar.api.response.UserResponse;
import com.paradoxdevs.dollar.entity.Role;
import com.paradoxdevs.dollar.entity.User;
import com.paradoxdevs.dollar.error.exception.ResourceNotFoundException;
import com.paradoxdevs.dollar.mapper.UserMapper;
import com.paradoxdevs.dollar.repository.UserRepository;
import com.paradoxdevs.dollar.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::entityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::entityToResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::entityToResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public UserResponse getUserByUuid(String uuid) {
        UUID queryUuid = UUID.fromString(uuid);
        return userRepository.findByUuid(queryUuid)
                .map(userMapper::entityToResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public void makeAdmin(String uuid) {
        modifyRole(uuid, Role.ADMIN, true);
    }

    @Override
    public void demoteAdmin(String uuid) {
        modifyRole(uuid, Role.ADMIN, false);
    }

    @Override
    public void restrictUser(String uuid) {
        modifyRole(uuid, Role.RESTRICTED, true);
    }

    @Override
    public void allowUser(String uuid) {
        modifyRole(uuid, Role.RESTRICTED, false);
    }

    @Override
    public void banUser(String uuid) {
        modifyRole(uuid, Role.BANNED, true);
    }

    @Override
    public void unbanUser(String uuid) {
        modifyRole(uuid, Role.BANNED, false);
    }

    @Override
    public void resetUser(String uuid) {
        UUID queryUuid = UUID.fromString(uuid);
        User user = userRepository.findByUuid(queryUuid).orElseThrow(ResourceNotFoundException::new);
        user.getRoles().clear();
        user.addRole(Role.USER);
        userRepository.save(user);
    }

    private void modifyRole(String uuid, Role role, boolean add) {
        UUID queryUuid = UUID.fromString(uuid);
        User user = userRepository.findByUuid(queryUuid).orElseThrow(ResourceNotFoundException::new);
        if (add) {
            user.addRole(role);
        } else {
            user.removeRole(role);
        }
        userRepository.save(user);
    }
}
