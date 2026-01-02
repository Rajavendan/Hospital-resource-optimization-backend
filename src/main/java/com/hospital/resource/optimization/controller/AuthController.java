package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.dto.JwtAuthResponse;
import com.hospital.resource.optimization.dto.LoginDto;
import com.hospital.resource.optimization.dto.RegisterDto;
import com.hospital.resource.optimization.dto.GoogleLoginDto;
import com.hospital.resource.optimization.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        JwtAuthResponse response = authService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<JwtAuthResponse> googleLogin(@RequestBody GoogleLoginDto googleLoginDto) {
        JwtAuthResponse response = authService.googleLogin(googleLoginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        String response = authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
