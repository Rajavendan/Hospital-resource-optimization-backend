package com.hospital.resource.optimization.service;

import com.hospital.resource.optimization.model.*;
import com.hospital.resource.optimization.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class ResourceAllocationService {

    private final BedRepository bedRepository;
    private final DoctorRepository doctorRepository;
    private final EquipmentRepository equipmentRepository;
    private final AlertRepository alertRepository;
    private final NotificationService notificationService;

    public ResourceAllocationService(BedRepository bedRepository, DoctorRepository doctorRepository,
            EquipmentRepository equipmentRepository, AlertRepository alertRepository,
            NotificationService notificationService) {
        this.bedRepository = bedRepository;
        this.doctorRepository = doctorRepository;
        this.equipmentRepository = equipmentRepository;
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public String allocateResources(Patient patient) {
        // 1. Allocate Doctor (Lowest Workload)
        Doctor assignedDoctor = doctorRepository.findAll().stream()
                .min(Comparator.comparingInt(Doctor::getCurrentWorkload))
                .orElseThrow(() -> new RuntimeException("No doctors available"));

        if (assignedDoctor.getCurrentWorkload() >= assignedDoctor.getMaxLoad()) {
            createAlert("CRITICAL", "Doctor Overload: No doctors available for patient " + patient.getId());
            notificationService.sendEmail("staff@hospital.com", "Doctor Overload", "Immediate attention required.");
        }

        assignedDoctor.setCurrentWorkload(assignedDoctor.getCurrentWorkload() + 1);
        doctorRepository.save(assignedDoctor);

        // 2. Allocate Bed (Based on Severity/Ward)
        String ward = determineWard(patient.getSeverity());
        Bed assignedBed = bedRepository.findByWardAndStatus(ward, "AVAILABLE").stream()
                .findFirst()
                .orElse(null);

        if (assignedBed == null) {
            createAlert("CRITICAL", "Bed Shortage: No " + ward + " beds available");
            notificationService.sendSms("9999999999", "Bed Shortage in " + ward);
            throw new RuntimeException("No beds available in " + ward);
        }

        assignedBed.setStatus("OCCUPIED");
        assignedBed.setPatient(patient);
        bedRepository.save(assignedBed);

        // 3. Allocate Equipment (If severity high)
        // 3. Allocate Equipment (If severity high, assign a Ventilator if available)
        if (patient.getSeverity() >= 4) {
            // Updated logic to match new Equipment entity (no queue length)
            // Ideally we look for specific equipment like Ventilator
            Equipment equipment = equipmentRepository.findByType("Critical Care").stream()
                    .filter(e -> "AVAILABLE".equals(e.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (equipment != null) {
                equipment.setStatus("IN_USE");
                equipmentRepository.save(equipment);
            } else {
                createAlert("INVENTORY", "Equipment Shortage (Critical Care) for Patient " + patient.getId());
            }
        }

        return "Resources allocated for patient " + patient.getName() +
                ". Doctor: " + assignedDoctor.getName() +
                ", Bed: " + assignedBed.getBedNumber();
    }

    private String determineWard(int severity) {
        if (severity >= 4)
            return "ICU";
        if (severity == 3)
            return "EMERGENCY";
        return "GENERAL";
    }

    private void createAlert(String type, String message) {
        Alert alert = new Alert();
        alert.setType(type);
        alert.setMessage(message);
        alert.setTimestamp(java.time.LocalDateTime.now());
        alert.setResolved(false);
        alertRepository.save(alert);
    }
}
