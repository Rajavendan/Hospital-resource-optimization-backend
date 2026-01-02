package com.hospital.resource.optimization.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customId; // e.g., PAT--01

    private String name;
    private int age;
    private String gender;
    private String contact;
    private String email;
    private String bloodGroup;

    private int severity; // 1-5
    private String diagnosis;
    private LocalDate admissionDate;

    // Optional link to User if they are a registered user
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String status; // ADMITTED, DISCHARGED, WAITING

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_doctor_id")
    private Doctor assignedDoctor;
}
