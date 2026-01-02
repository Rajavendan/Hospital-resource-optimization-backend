package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
        List<Appointment> findByDoctorId(Long doctorId);

        List<Appointment> findByPatientId(Long patientId);

        List<Appointment> findByDoctor(com.hospital.resource.optimization.model.Doctor doctor);

        long countByDoctorAndDateAndTime(com.hospital.resource.optimization.model.Doctor doctor,
                        java.time.LocalDate date,
                        java.time.LocalTime time);

        List<Appointment> findByDoctorAndDate(com.hospital.resource.optimization.model.Doctor doctor,
                        java.time.LocalDate date);

        void deleteByPatientId(Long patientId);

        List<Appointment> findByDate(java.time.LocalDate date);
}
