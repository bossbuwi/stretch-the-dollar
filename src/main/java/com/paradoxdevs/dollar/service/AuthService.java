package com.paradoxdevs.dollar.service;

import com.paradoxdevs.dollar.api.request.AuthRequest;
import com.paradoxdevs.dollar.api.request.PasswordRequest;
import com.paradoxdevs.dollar.api.response.AuthResponse;
import com.paradoxdevs.dollar.api.response.UserResponse;

public interface AuthService {
    UserResponse register(AuthRequest request);
    AuthResponse login(AuthRequest request);
    void forgetPassword(AuthRequest request);
    void changePassword(PasswordRequest request);
}
