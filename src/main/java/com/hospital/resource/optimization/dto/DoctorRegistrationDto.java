package com.hospital.resource.optimization.dto;

import lombok.Data;

@Data
public class DoctorRegistrationDto {
    private String name;
    private String email; // Will be used as username
    private String password;
    private String specialization;
    private String shiftStartTime;
    private String shiftEndTime;
    private Integer maxLoad;
    private String phoneNumber;
}
