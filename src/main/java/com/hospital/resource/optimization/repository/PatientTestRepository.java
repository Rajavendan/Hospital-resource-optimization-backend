package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.PatientTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientTestRepository extends JpaRepository<PatientTest, Long> {
    List<PatientTest> findByStatus(String status);

    List<PatientTest> findByTestIdAndStatus(Long testId, String status);

    void deleteByPatientId(Long patientId);
}
