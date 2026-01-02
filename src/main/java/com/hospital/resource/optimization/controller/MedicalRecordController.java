package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.model.MedicalRecord;
import com.hospital.resource.optimization.repository.MedicalRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordRepository repository;

    public MedicalRecordController(MedicalRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/patient/{patientId}")
    public List<MedicalRecord> getRecordsByPatient(@PathVariable Long patientId) {
        return repository.findByPatientId(patientId);
    }

    @PostMapping
    public MedicalRecord addRecord(@RequestBody MedicalRecord record) {
        record.setDate(java.time.LocalDateTime.now());
        return repository.save(record);
    }
}
