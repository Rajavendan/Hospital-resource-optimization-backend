package com.hospital.resource.optimization.service;

import com.hospital.resource.optimization.model.Doctor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorService {
    List<Doctor> getAllDoctors();

    Optional<Doctor> getDoctorById(Long id);

    Doctor saveDoctor(Doctor doctor);

    List<Doctor> getAvailableDoctors(LocalDate date, LocalTime time);

    Long getDoctorIdByUserId(Long userId);

    void deleteDoctor(Long id);

    List<Doctor> getDoctorsBySpecialization(String specialization);
}
