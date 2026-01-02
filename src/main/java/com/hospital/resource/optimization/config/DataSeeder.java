package com.hospital.resource.optimization.config;

import com.hospital.resource.optimization.model.Doctor;
import com.hospital.resource.optimization.model.Role;
import com.hospital.resource.optimization.model.Staff;
import com.hospital.resource.optimization.model.User;
import com.hospital.resource.optimization.repository.DoctorRepository;
import com.hospital.resource.optimization.repository.StaffRepository;
import com.hospital.resource.optimization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedDoctors();
        seedStaff();
    }

    private void seedDoctors() {
        if (doctorRepository.count() >= 10)
            return;

        String[] departments = { "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Dermatology" };
        String[] shifts = { "08:00", "16:00", "16:00", "23:00" }; // Day and Evening shifts

        for (int i = 1; i <= 10; i++) {
            String email = "doctor" + i + "@hospital.com";
            if (userRepository.existsByUsername(email))
                continue;

            // Create User
            User user = new User();
            user.setName("Dr. Test Doctor " + i);
            user.setUsername(email);
            user.setPassword(passwordEncoder.encode("password123")); // Default password
            user.setRole(Role.DOCTOR);
            userRepository.save(user);

            // Create Doctor
            Doctor doctor = new Doctor();
            doctor.setName(user.getName());
            doctor.setEmail(email);
            doctor.setSpecialization(departments[i % departments.length]);
            doctor.setPhoneNumber("555-010" + i);

            // Assign shifts purely for demo
            String shiftStart = shifts[i % 2 == 0 ? 0 : 2]; // 08:00 or 16:00
            String shiftEnd = shifts[i % 2 == 0 ? 1 : 3]; // 16:00 or 23:00

            doctor.setShiftStartTime(shiftStart);
            doctor.setShiftEndTime(shiftEnd);
            doctor.setMaxLoad(15);
            doctor.setCurrentWorkload(0);
            doctor.setUser(user);
            doctor.setCustomId(String.format("DOC--%02d", i));

            doctorRepository.save(doctor);
            System.out.println("Seeded Doctor: " + doctor.getName());
        }
    }

    private void seedStaff() {
        if (staffRepository.count() >= 10)
            return;

        for (int i = 1; i <= 10; i++) {
            String email = "staff" + i + "@hospital.com";
            if (userRepository.existsByUsername(email))
                continue;

            // Create User
            User user = new User();
            user.setName("Staff Member " + i);
            user.setUsername(email);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.STAFF);
            userRepository.save(user);

            // Create Staff
            Staff staff = new Staff();
            staff.setName(user.getName());
            staff.setEmail(email);
            staff.setRole("Nurse"); // Or Receptionist
            staff.setDepartment(i % 2 == 0 ? "Emergency" : "General Ward");
            staff.setShift(i % 2 == 0 ? "Morning" : "Night");
            staff.setPhoneNumber("555-020" + i);
            staff.setAddress("123 Hospital Lane");
            staff.setUser(user);
            staff.setCustomId(String.format("STF--%02d", i));

            staffRepository.save(staff);
            System.out.println("Seeded Staff: " + staff.getName());
        }
    }
}
