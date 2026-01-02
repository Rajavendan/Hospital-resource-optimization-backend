package com.hospital.resource.optimization.service;

import com.hospital.resource.optimization.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientService {
    List<Patient> getAllPatients();

    Patient registerPatient(Patient patient);

    Optional<Patient> getPatientById(Long id);

    void deletePatient(Long id);
}
