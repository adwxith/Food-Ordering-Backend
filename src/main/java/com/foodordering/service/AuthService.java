package com.foodordering.service;

import com.foodordering.dto.request.LoginRequest;
import com.foodordering.dto.request.RegisterRequest;
import com.foodordering.dto.response.JwtAuthenticationResponse;

public interface AuthService {
    JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest);
    Long registerUser(RegisterRequest registerRequest);
}