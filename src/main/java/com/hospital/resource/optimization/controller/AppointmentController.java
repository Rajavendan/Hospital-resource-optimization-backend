package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.model.Appointment;
import com.hospital.resource.optimization.repository.AppointmentRepository;
import com.hospital.resource.optimization.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final com.hospital.resource.optimization.service.AppointmentService appointmentService;

    public AppointmentController(com.hospital.resource.optimization.service.AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    // Get logged-in doctor's appointments
    // Get logged-in doctor's appointments
    // Get logged-in doctor's appointments
    @GetMapping("/my-schedule")
    public List<Appointment> getMyAppointments(org.springframework.security.core.Authentication authentication) {
        com.hospital.resource.optimization.config.CustomUserDetails userDetails = (com.hospital.resource.optimization.config.CustomUserDetails) authentication
                .getPrincipal(); // This has the User ID

        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (isDoctor) {
            try {
                // Find doctor profile via User ID
                // Note: We need to inject DoctorService here, or expose the lookup in
                // AppointmentService.
                // Assuming AppointmentService has logic or we inject DoctorRepository.
                // Since avoiding massive refactor, let's use the existing getDoctorAppointments
                // that takes "username" (email).
                // Ideally, we should use ID.
                return appointmentService.getDoctorAppointments(userDetails.getUsername());
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Patient logic
            return appointmentService.getPatientAppointments(userDetails.getId());
        }
    }

    @PostMapping("/book")
    public ResponseEntity<Appointment> bookAppointment(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.bookAppointment(appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/slots")
    public List<java.time.LocalTime> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return appointmentService.getAvailableSlots(doctorId, date);
    }
}
