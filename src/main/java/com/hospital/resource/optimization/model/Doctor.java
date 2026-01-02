package com.hospital.resource.optimization.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customId; // e.g., DOC--01

    private String name;
    private String specialization;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    private int currentWorkload;
    private int maxLoad = 10;

    private String shiftStartTime; // e.g. "09:00"
    private String shiftEndTime; // e.g. "17:00"

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
