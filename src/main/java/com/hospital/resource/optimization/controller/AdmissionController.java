package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.model.Bed;
import com.hospital.resource.optimization.model.Patient;
import com.hospital.resource.optimization.repository.PatientRepository;
import com.hospital.resource.optimization.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admission")
@PreAuthorize("hasRole('STAFF')")
public class AdmissionController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private BedService bedService;

    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return ResponseEntity.ok(patients);
    }

    @PostMapping("/admit")
    @Transactional
    public ResponseEntity<?> admitPatient(@RequestBody Map<String, Object> admissionData) {
        try {
            // Extract data
            String name = (String) admissionData.get("name");
            String ward = (String) admissionData.get("ward");
            
            // Create Patient with all fields
            Patient patient = new Patient();
            patient.setName(name);
            patient.setAdmissionDate(LocalDate.now());
            patient.setStatus("ADMITTED");
            
            // Set additional fields from form data
            if (admissionData.get("age") != null) {
                patient.setAge(admissionData.get("age") instanceof Integer 
                    ? (Integer) admissionData.get("age") 
                    : Integer.parseInt(admissionData.get("age").toString()));
            }
            if (admissionData.get("gender") != null) {
                patient.setGender(admissionData.get("gender").toString());
            }
            if (admissionData.get("contact") != null) {
                patient.setContact(admissionData.get("contact").toString());
            }
            if (admissionData.get("severity") != null) {
                patient.setSeverity(admissionData.get("severity") instanceof Integer 
                    ? (Integer) admissionData.get("severity") 
                    : Integer.parseInt(admissionData.get("severity").toString()));
            }
            if (admissionData.get("diagnosis") != null) {
                patient.setDiagnosis(admissionData.get("diagnosis").toString());
            }

            Patient savedPatient = patientRepository.save(patient);

            // Auto-Assign Bed
            Bed assignedBed = bedService.assignBed(ward);
            assignedBed.setPatient(savedPatient);
            bedService.updateBed(assignedBed.getId(), assignedBed); // Persist constraint

            return ResponseEntity.ok(Map.of(
                    "message", "Patient Admitted Successfully",
                    "patient", savedPatient,
                    "bed", assignedBed));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
