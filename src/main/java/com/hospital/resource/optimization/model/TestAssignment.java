package com.hospital.resource.optimization.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_assignments")
public class TestAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    private String status; // WAITING_FOR_PAYMENT, PENDING (Ready for test), COMPLETED
    private String paymentStatus; // PENDING, PAID
    private String reportPath; // Path or URL to the uploaded report

    private LocalDateTime assignedDate;
    private LocalDateTime completedDate;

    @PrePersist
    protected void onCreate() {
        assignedDate = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}
