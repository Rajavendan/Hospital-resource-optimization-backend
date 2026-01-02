package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findBySeverityGreaterThanEqual(int severity);

    java.util.Optional<Patient> findByUserId(Long userId);

    List<Patient> findByAssignedDoctorId(Long doctorId);
}
