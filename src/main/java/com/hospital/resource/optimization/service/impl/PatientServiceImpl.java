package com.hospital.resource.optimization.service.impl;

import com.hospital.resource.optimization.model.Patient;
import com.hospital.resource.optimization.repository.PatientRepository;
import com.hospital.resource.optimization.service.NotificationService;
import com.hospital.resource.optimization.service.PatientService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final NotificationService notificationService;

    private final com.hospital.resource.optimization.repository.UserRepository userRepository;
    private final com.hospital.resource.optimization.repository.PatientTestRepository patientTestRepository;
    private final com.hospital.resource.optimization.repository.BedRepository bedRepository;
    private final com.hospital.resource.optimization.repository.AppointmentRepository appointmentRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public PatientServiceImpl(PatientRepository patientRepository,
            NotificationService notificationService,
            com.hospital.resource.optimization.repository.UserRepository userRepository,
            com.hospital.resource.optimization.repository.PatientTestRepository patientTestRepository,
            com.hospital.resource.optimization.repository.BedRepository bedRepository,
            com.hospital.resource.optimization.repository.AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.patientTestRepository = patientTestRepository;
        this.bedRepository = bedRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient registerPatient(Patient patient) {
        Patient saved = patientRepository.save(patient);
        if (saved.getContact() != null) {
            try {
                notificationService.sendSms(saved.getContact(), "Welcome to Smart Hospital! Registration Successful.");
            } catch (Exception e) {
                System.err.println("Failed to send welcome SMS: " + e.getMessage());
            }
        }
        return saved;
    }

    @Override
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // 1. Cleanup Tests
        patientTestRepository.deleteByPatientId(id);

        // 2. Free Bed
        List<com.hospital.resource.optimization.model.Bed> beds = bedRepository.findByStatus("OCCUPIED");
        for (com.hospital.resource.optimization.model.Bed bed : beds) {
            if (bed.getPatient() != null && bed.getPatient().getId().equals(id)) {
                bed.setPatient(null);
                bed.setStatus("COOLING");
                bed.setLastDischargedAt(java.time.LocalDateTime.now());
                bedRepository.save(bed);
            }
        }

        // 3. Delete Appointments
        appointmentRepository.deleteByPatientId(id);

        // 4. Delete User linked
        if (patient.getUser() != null) {
            userRepository.delete(patient.getUser());
        }

        // 5. Delete Patient
        patientRepository.delete(patient);
    }
}
