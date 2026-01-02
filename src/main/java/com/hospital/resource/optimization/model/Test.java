package com.hospital.resource.optimization.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tests")
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Blood Test", "MRI"

    private String department; // e.g., "Pathology", "Radiology"

    private double cost;

    private int maxCapacity;

    private String description;

    // Status: ACTIVE, INACTIVE
    @Column(nullable = false)
    private String status = "ACTIVE";

    @jakarta.persistence.Transient
    private int currentCount; // Daily limit

    // Helper fields if needed for UI, but entity should be simple
}
