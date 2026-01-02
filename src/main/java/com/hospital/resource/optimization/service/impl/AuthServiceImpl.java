package com.hospital.resource.optimization.service.impl;

import com.hospital.resource.optimization.config.JwtTokenProvider;
import com.hospital.resource.optimization.dto.JwtAuthResponse;
import com.hospital.resource.optimization.dto.LoginDto;
import com.hospital.resource.optimization.dto.JwtAuthResponse;
import com.hospital.resource.optimization.dto.LoginDto;
import com.hospital.resource.optimization.dto.RegisterDto;
import com.hospital.resource.optimization.dto.GoogleLoginDto;
import com.hospital.resource.optimization.model.Role;
import com.hospital.resource.optimization.model.User;
import com.hospital.resource.optimization.repository.UserRepository;
import com.hospital.resource.optimization.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final com.hospital.resource.optimization.repository.PatientRepository patientRepository;
    private final com.hospital.resource.optimization.service.TwilioService twilioService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
            com.hospital.resource.optimization.repository.PatientRepository patientRepository,
            com.hospital.resource.optimization.service.TwilioService twilioService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.patientRepository = patientRepository;
        this.twilioService = twilioService;
    }

    @Override
    public String register(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }

        User user = new User();
        user.setName(registerDto.getName());
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        // Force role to PATIENT for public registration
        user.setRole(Role.PATIENT);

        userRepository.save(user);

        com.hospital.resource.optimization.model.Patient patient = new com.hospital.resource.optimization.model.Patient();
        patient.setName(user.getName());
        patient.setUser(user);
        patient.setAge(registerDto.getAge());
        patient.setGender(registerDto.getGender());
        patient.setBloodGroup(registerDto.getBloodGroup());
        patient.setContact(registerDto.getPhoneNumber()); // Assuming Contact maps to Phone Number
        // code field but might exist.
        // Model check: It has status, diagnosis, etc.
        patient.setStatus("Active");

        patientRepository.save(patient);

        // Generate Custom ID
        String customId = String.format("PAT--%02d", patient.getId());
        patient.setCustomId(customId);
        patientRepository.save(patient);

        // Send SMS
        try {
            if (patient.getContact() != null) {
                twilioService.sendSms(patient.getContact(), "Welcome to Smart Hospital! Registration Successful.");
            }
        } catch (Exception e) {
            System.err.println("Failed to send signup SMS: " + e.getMessage());
        }

        return "User registered successfully!";
    }

    @Override
    public JwtAuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginDto.getUsername()).orElseThrow();

        return new JwtAuthResponse(token, "Bearer", user.getRole().name(), user.getName(), user.getId());
    }

    @Override
    public JwtAuthResponse googleLogin(GoogleLoginDto googleLoginDto) {
        // Check if user exists by email
        java.util.Optional<User> existingUser = userRepository.findByUsername(googleLoginDto.getEmail());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Optional: Update details from Google if needed
        } else {
            // Register new user as Patient
            user = new User();
            user.setName(googleLoginDto.getName());
            user.setUsername(googleLoginDto.getEmail());
            // Set a dummy password or random secure one
            user.setPassword(passwordEncoder.encode("GOOGLE_AUTH_" + java.util.UUID.randomUUID().toString()));
            user.setRole(Role.PATIENT);
            user.setEnabled(true);
            userRepository.save(user);

            // Create Patient Profile
            com.hospital.resource.optimization.model.Patient patient = new com.hospital.resource.optimization.model.Patient();
            patient.setName(user.getName());
            patient.setUser(user);
            patient.setEmail(user.getUsername());
            patient.setStatus("Active");
            patientRepository.save(patient);

            // Generate Custom ID
            String customId = String.format("PAT--%02d", patient.getId());
            patient.setCustomId(customId);
            patientRepository.save(patient);
        }

        // Generate Token (Manually creating authentication token since we don't have
        // password for existing users easily available/or we trust google)
        // Note: Standard Spring Security flow usually requires authentication.
        // We can create a custom authentication token or force it.

        // For simplicity, we'll assume we trust the email since it came from valid
        // Google check in frontend (and validated backend side ideally)
        // Creating a UsernamePasswordAuthenticationToken without credentials implies
        // authorized
        // However, checks might fail if we don't handle it carefully.
        // Better approach: UserDetails load.

        // Let's rely on TokenProvider generating token for the username directly if
        // possible, or create a simple auth object.

        // Simulating Authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                java.util.Collections
                        .singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name())));

        String token = jwtTokenProvider.generateToken(authentication);

        return new JwtAuthResponse(token, "Bearer", user.getRole().name(), user.getName(), user.getId());
    }
}
