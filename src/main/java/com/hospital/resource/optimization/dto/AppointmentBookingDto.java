package com.hospital.resource.optimization.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentBookingDto {
    private Long patientId;
    private String specialization;
    private LocalDate date;
    private LocalTime time;
}

