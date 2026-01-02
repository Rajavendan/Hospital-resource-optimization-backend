package com.hospital.resource.optimization.config;

import com.hospital.resource.optimization.model.Doctor;
import com.hospital.resource.optimization.model.Role;
import com.hospital.resource.optimization.model.User;
import com.hospital.resource.optimization.repository.DoctorRepository;
import com.hospital.resource.optimization.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, DoctorRepository doctorRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Disabled sample doctor creation as per user request
        /*
         * if (doctorRepository.count() == 0) {
         * System.out.println("Initializing 50 Doctors...");
         * 
         * String[] specializations = { "Cardiologist", "Neurologist", "Orthopedic",
         * "Pediatrician", "Dermatologist",
         * "General Physician" };
         * String[] shiftStarts = { "08:00", "09:00", "14:00", "18:00" }; // Morning,
         * Morning, Afternoon, Evening
         * Random random = new Random();
         * 
         * for (int i = 1; i <= 50; i++) {
         * String name = "Doctor " + i;
         * String email = "doctor" + i + "@hospital.com";
         * String password = "password123";
         * 
         * // Use specified emails for first 3 doctors for testing
         * if (i == 1)
         * email = "rajurajaidc@gmail.com";
         * else if (i == 2)
         * email = "rajavendan.it22@bitsathy.ac.in";
         * else if (i == 3)
         * email = "rajabing003@gmail.com";
         * 
         * if (!userRepository.existsByUsername(email)) {
         * // Create User Logic
         * User user = new User();
         * user.setName(name);
         * user.setUsername(email);
         * user.setPassword(passwordEncoder.encode(password));
         * user.setRole(Role.DOCTOR);
         * userRepository.save(user);
         * 
         * // Create Doctor Logic
         * Doctor doctor = new Doctor();
         * doctor.setName(name);
         * doctor.setEmail(email);
         * doctor.setSpecialization(specializations[random.nextInt(specializations.
         * length)]);
         * doctor.setCurrentWorkload(0);
         * doctor.setMaxLoad(10 + random.nextInt(5)); // 10-15 max load
         * 
         * String start = shiftStarts[random.nextInt(shiftStarts.length)];
         * doctor.setShiftStartTime(start);
         * // Simple logic: 8 hour shift
         * int startHour = Integer.parseInt(start.split(":")[0]);
         * int endHour = (startHour + 8) % 24;
         * doctor.setShiftEndTime(String.format("%02d:00", endHour));
         * 
         * doctor.setUser(user);
         * doctorRepository.save(doctor);
         * }
         * }
         * System.out.println("50 Doctors Initialized Successfully.");
         * }
         */
        // Seed Admin User
        if (!userRepository.existsByUsername("admin@hospital.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setUsername("admin@hospital.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("Admin Initialized: admin@hospital.com / admin123");
        }
    }
}
