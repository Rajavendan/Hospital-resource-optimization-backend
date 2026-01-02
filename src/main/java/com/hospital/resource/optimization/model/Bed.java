package com.hospital.resource.optimization.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "beds")
public class Bed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bedNumber;

    @Column(nullable = false)
    private String ward; // ICU, GENERAL, EMERGENCY

    @Column(nullable = false)
    private String status; // AVAILABLE, UNAVAILABLE, OCCUPIED

    @OneToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private java.time.LocalDateTime lastDischargedAt;
}
