package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.model.Patient;
import com.hospital.resource.optimization.repository.BedRepository;
import com.hospital.resource.optimization.repository.EquipmentRepository;
import com.hospital.resource.optimization.service.ResourceAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceAllocationService resourceAllocationService;
    private final BedRepository bedRepository;
    private final EquipmentRepository equipmentRepository;

    public ResourceController(ResourceAllocationService resourceAllocationService, BedRepository bedRepository,
            EquipmentRepository equipmentRepository) {
        this.resourceAllocationService = resourceAllocationService;
        this.bedRepository = bedRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @PostMapping("/allocate")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<String> allocateResources(@RequestBody Patient patient) {
        String result = resourceAllocationService.allocateResources(patient);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/beds")
    public ResponseEntity<?> getAllBeds() {
        return ResponseEntity.ok(bedRepository.findAll());
    }

    @GetMapping("/equipment")
    public ResponseEntity<?> getAllEquipment() {
        return ResponseEntity.ok(equipmentRepository.findAll());
    }
}
