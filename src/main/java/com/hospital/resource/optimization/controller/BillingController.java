package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.model.TestAssignment;
import com.hospital.resource.optimization.repository.TestAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@PreAuthorize("hasRole('BILLING')")
public class BillingController {

    @Autowired
    private TestAssignmentRepository testAssignmentRepository;

    @GetMapping("/pending")
    public ResponseEntity<List<TestAssignment>> getPendingPayments() {
        return ResponseEntity.ok(testAssignmentRepository.findByStatus("WAITING_FOR_PAYMENT"));
    }

    @PostMapping("/pay/{id}")
    public ResponseEntity<TestAssignment> markAsPaid(@PathVariable Long id) {
        return testAssignmentRepository.findById(id)
                .map(assignment -> {
                    assignment.setPaymentStatus("PAID");
                    assignment.setStatus("PENDING"); // Ready for test execution
                    return ResponseEntity.ok(testAssignmentRepository.save(assignment));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
