package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.model.Alert;
import com.hospital.resource.optimization.repository.AlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRepository repository;

    public AlertController(AlertRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/active")
    public List<Alert> getActiveAlerts() {
        return repository.findByResolvedFalse();
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        return repository.findById(id).map(alert -> {
            alert.setResolved(true);
            return ResponseEntity.ok(repository.save(alert));
        }).orElse(ResponseEntity.notFound().build());
    }
}
