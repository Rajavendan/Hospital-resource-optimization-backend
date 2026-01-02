package com.hospital.resource.optimization.service;

import com.hospital.resource.optimization.dto.JwtAuthResponse;
import com.hospital.resource.optimization.dto.LoginDto;
import com.hospital.resource.optimization.dto.RegisterDto;

public interface AuthService {
    String register(RegisterDto registerDto);

    JwtAuthResponse login(LoginDto loginDto);

    JwtAuthResponse googleLogin(com.hospital.resource.optimization.dto.GoogleLoginDto googleLoginDto);
}
