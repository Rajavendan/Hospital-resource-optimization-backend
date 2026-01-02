package com.hospital.resource.optimization.service.impl;

import com.hospital.resource.optimization.model.Appointment;
import com.hospital.resource.optimization.model.Doctor;
import com.hospital.resource.optimization.model.Patient;
import com.hospital.resource.optimization.repository.AppointmentRepository;
import com.hospital.resource.optimization.repository.DoctorRepository;
import com.hospital.resource.optimization.repository.PatientRepository;
import com.hospital.resource.optimization.repository.UserRepository;
import com.hospital.resource.optimization.service.AppointmentService;
import com.hospital.resource.optimization.service.EmailService;
import com.hospital.resource.optimization.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public List<LocalTime> getAvailableSlots(Long doctorId, java.time.LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (doctor.getShiftStartTime() == null || doctor.getShiftEndTime() == null) {
            return List.of();
        }

        LocalTime start = LocalTime.parse(doctor.getShiftStartTime());
        LocalTime end = LocalTime.parse(doctor.getShiftEndTime());
        List<LocalTime> availableSlots = new java.util.ArrayList<>();

        // Generate 1-hour slots
        LocalTime current = start;
        while (current.isBefore(end)) {
            // Check availability
            long count = appointmentRepository.countByDoctorAndDateAndTime(doctor, date, current);
            if (count < 4) {
                availableSlots.add(current);
            }
            current = current.plusHours(1);
        }

        return availableSlots;
    }

    @Override
    public List<Appointment> getDoctorAppointments(String username) {
        // Assuming doctor's email is username
        Doctor doctor = doctorRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return appointmentRepository.findByDoctor(doctor);
    }

    @Override
    public List<Appointment> getPatientAppointments(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user ID: " + userId));
        return appointmentRepository.findByPatientId(patient.getId());
    }

    @Override
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        // Handle patient - if patient has only ID (from user), find or create patient
        // record
        if (appointment.getPatient() != null && appointment.getPatient().getId() != null) {
            Patient patient = patientRepository.findById(appointment.getPatient().getId()).orElse(null);
            if (patient == null) {
                // Try to find patient by user ID
                patient = patientRepository.findAll().stream()
                        .filter(p -> p.getUser() != null
                                && p.getUser().getId().equals(appointment.getPatient().getId()))
                        .findFirst()
                        .orElse(null);
                if (patient == null) {
                    // Create a new patient record linked to user
                    patient = new Patient();
                    com.hospital.resource.optimization.model.User user = userRepository
                            .findById(appointment.getPatient().getId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    patient.setUser(user);
                    patient.setName(user.getName());
                    patient.setStatus("WAITING");
                    patient = patientRepository.save(patient);
                }
            }
            appointment.setPatient(patient);
        }

        // If appointment has a doctor already set, use it (for backward compatibility)
        if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
            Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            // Validate shift time
            if (!isWithinShift(doctor, appointment.getTime())) {
                throw new RuntimeException(
                        "Doctor " + doctor.getName() + " is not available at " + appointment.getTime() + ". Shift: "
                                + doctor.getShiftStartTime() + " - " + doctor.getShiftEndTime());
            }

            // Check workload
            if (doctor.getCurrentWorkload() >= doctor.getMaxLoad()) {
                throw new RuntimeException("Doctor " + doctor.getName() + " has reached maximum workload ("
                        + doctor.getMaxLoad() + " appointments).");
            }

            doctor.setCurrentWorkload(doctor.getCurrentWorkload() + 1);
            doctorRepository.save(doctor);

            appointment.setDoctor(doctor);
            appointment.setStatus("SCHEDULED");
            Appointment saved = appointmentRepository.save(appointment);
            sendDoctorEmail(doctor, saved);
            return saved;
        }

        // 1. Filter Doctors by Specialization (Default to General Physician if not
        // specified)
        String requiredSpec = appointment.getDoctor() != null && appointment.getDoctor().getSpecialization() != null
                ? appointment.getDoctor().getSpecialization()
                : "General Physician";

        List<Doctor> doctors = doctorRepository.findBySpecialization(requiredSpec);
        if (doctors.isEmpty()) {
            throw new RuntimeException("No doctors available for specialization: " + requiredSpec);
        }

        // 2. Filter by Shift Time
        LocalTime apptTime = appointment.getTime();
        List<Doctor> shiftDoctors = doctors.stream()
                .filter(d -> isWithinShift(d, apptTime))
                .toList();

        if (shiftDoctors.isEmpty()) {
            throw new RuntimeException("No doctors available at " + apptTime);
        }

        // 3. Load Balancing: Select doctor with lowest current workload < maxLoad
        Doctor bestDoctor = shiftDoctors.stream()
                .filter(d -> d.getCurrentWorkload() < d.getMaxLoad())
                // Check daily slot limit (Assuming 4 per slot, but if auto-assigning, we need a
                // specific slot; here we are just load balancing for general?)
                // Wait, if patient doesn't select doctor, we assign one. We should ensure the
                // ASSIGNED doctor has space in that slot.
                .filter(d -> appointmentRepository.countByDoctorAndDateAndTime(d, appointment.getDate(),
                        appointment.getTime()) < 4)
                .min(Comparator.comparingInt(Doctor::getCurrentWorkload))
                .orElseThrow(() -> new RuntimeException("All doctors are fully booked for this slot."));

        // 4. Assign & Update
        bestDoctor.setCurrentWorkload(bestDoctor.getCurrentWorkload() + 1);
        doctorRepository.save(bestDoctor);

        appointment.setDoctor(bestDoctor);
        appointment.setStatus("SCHEDULED");
        Appointment saved = appointmentRepository.save(appointment);

        // 5. Notifications
        // SMS to Patient
        try {
            if (saved.getPatient() != null && saved.getPatient().getContact() != null) {
                String msg = "Appointment Confirmed with Dr. " + bestDoctor.getName() + " on " + saved.getDate();
                notificationService.sendSms(saved.getPatient().getContact(), msg);
            }
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }

        // Email to Doctor
        sendDoctorEmail(bestDoctor, saved);

        return saved;
    }

    private boolean isWithinShift(Doctor doctor, LocalTime time) {
        if (doctor.getShiftStartTime() == null || doctor.getShiftEndTime() == null)
            return true; // Default allow
        LocalTime start = LocalTime.parse(doctor.getShiftStartTime());
        LocalTime end = LocalTime.parse(doctor.getShiftEndTime());
        // Handle overnight shifts if needed, but for now simple comparison
        if (start.isBefore(end)) {
            return !time.isBefore(start) && time.isBefore(end);
        } else {
            // Overflow shift (e.g. 22:00 to 06:00)
            return !time.isBefore(start) || time.isBefore(end);
        }
    }

    private void sendDoctorEmail(Doctor doctor, Appointment appointment) {
        String subject = "New Appointment Assigned: " + appointment.getDate() + " " + appointment.getTime();
        String htmlBody = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }"
                +
                ".content { background-color: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; border-radius: 0 0 8px 8px; }"
                +
                "table { width: 100%; border-collapse: collapse; margin: 20px 0; background-color: white; }" +
                "th, td { padding: 12px; text-align: left; border-bottom: 1px solid #e5e7eb; }" +
                "th { background-color: #f3f4f6; font-weight: bold; color: #1f2937; }" +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; font-size: 12px; color: #6b7280; text-align: center; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Smart Hospital Resource Optimization</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>New Appointment Assigned</h2>" +
                "<p>Dear Dr. <strong>" + doctor.getName() + "</strong>,</p>" +
                "<p>You have a new appointment scheduled. Please find the details below:</p>" +
                "<table>" +
                "<tr><th>Date</th><td>" + appointment.getDate() + "</td></tr>" +
                "<tr><th>Time</th><td>" + appointment.getTime() + "</td></tr>" +
                "<tr><th>Patient Name</th><td>"
                + (appointment.getPatient() != null ? appointment.getPatient().getName() : "Walk-in") + "</td></tr>" +
                "<tr><th>Your Shift</th><td>" + doctor.getShiftStartTime() + " - " + doctor.getShiftEndTime()
                + "</td></tr>" +
                "</table>" +
                "<p style='margin-top: 20px;'>Please log in to your dashboard to view full details and manage your schedule.</p>"
                +
                "<div class='footer'>" +
                "<p>This is an automated notification from Smart Hospital Resource Optimization System.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        try {
            emailService.sendHtmlMessage(doctor.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            System.err.println("Failed to send appointment confirmation email: " + e.getMessage());
            // Intentionally swallow exception to not rollback transaction
        }
    }

    @Override
    public void cancelAppointment(Long id) {
        Appointment appt = appointmentRepository.findById(id).orElse(null);
        if (appt != null) {
            Doctor doc = appt.getDoctor();
            if (doc != null) {
                // Decrement workload
                doc.setCurrentWorkload(Math.max(0, doc.getCurrentWorkload() - 1));
                doctorRepository.save(doc);
            }
            appointmentRepository.deleteById(id);
        }
    }
}
