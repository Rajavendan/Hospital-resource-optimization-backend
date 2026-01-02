package com.hospital.resource.optimization.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;
    private String address;
    private String shift; // e.g., "Morning", "Night", "09:00-17:00"
    private String role; // e.g. Nurse, Receptionist
    private String department;

    private String customId; // e.g., STF--001

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
