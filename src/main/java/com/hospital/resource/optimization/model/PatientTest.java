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
@Table(name = "patient_tests")
public class PatientTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    private String status; // PENDING, COMPLETED, IN_PROGRESS

    @Column(name = "assigned_by_doctor_id")
    private Long assignedByDoctorId;

    @Column(name = "completed_by_staff_id")
    private Long completedByStaffId;

    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
}
