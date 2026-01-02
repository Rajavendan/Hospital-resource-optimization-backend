package com.hospital.resource.optimization.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // CRITICAL, INVENTORY, STAFFING
    private String message;
    private LocalDateTime timestamp;
    private boolean resolved;
}
