package com.hospital.resource.optimization.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String name;
    private String username;
    private String password;
    private String role; // DOCTOR, PATIENT, STAFF
    private int age;
    private String gender;
    private String bloodGroup;
    private String phoneNumber;
}
