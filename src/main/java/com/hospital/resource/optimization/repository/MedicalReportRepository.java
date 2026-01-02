package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
    List<MedicalReport> findByPatientId(Long patientId);

    List<MedicalReport> findByUploadedById(Long doctorId);
}
