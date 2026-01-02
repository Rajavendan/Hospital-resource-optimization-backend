package com.hospital.resource.optimization.service;

import com.hospital.resource.optimization.model.Appointment;
import java.util.List;

public interface AppointmentService {
    Appointment bookAppointment(Appointment appointment);

    List<Appointment> getAllAppointments();

    void cancelAppointment(Long id);

    List<Appointment> getDoctorAppointments(String username);

    List<Appointment> getPatientAppointments(Long userId);

    List<java.time.LocalTime> getAvailableSlots(Long doctorId, java.time.LocalDate date);
}
