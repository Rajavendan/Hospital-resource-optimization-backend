package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialization(String specialization);

    Optional<Doctor> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Doctor> findByUserId(Long userId);
}
