package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.model.Bed;
import com.hospital.resource.optimization.model.Equipment;
import com.hospital.resource.optimization.service.BedService;
import com.hospital.resource.optimization.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('STAFF')") // STRICT ACCESS CONTROL
public class StaffController {

    @Autowired
    private BedService bedService;

    @Autowired
    private com.hospital.resource.optimization.service.PatientService patientService;

    @Autowired
    private com.hospital.resource.optimization.repository.DoctorRepository doctorRepository;

    @Autowired
    private com.hospital.resource.optimization.repository.PatientRepository patientRepository;

    @Autowired
    private com.hospital.resource.optimization.repository.TestAssignmentRepository testAssignmentRepository;

    @Autowired
    private com.hospital.resource.optimization.repository.AppointmentRepository appointmentRepository;

    // --- OPD MANAGEMENT ---

    @PostMapping("/opd")
    public ResponseEntity<com.hospital.resource.optimization.model.Patient> registerOpdPatient(
            @RequestBody com.hospital.resource.optimization.model.Patient patient) {
        // 1. Auto-Assign Doctor (Load Balancing)
        List<com.hospital.resource.optimization.model.Doctor> doctors = doctorRepository.findAll();

        com.hospital.resource.optimization.model.Doctor bestDoctor = doctors.stream()
                .min(java.util.Comparator
                        .comparingInt(com.hospital.resource.optimization.model.Doctor::getCurrentWorkload))
                .orElseThrow(() -> new RuntimeException("No doctors available"));

        // 2. Set Patient Details
        patient.setAssignedDoctor(bestDoctor);
        patient.setStatus("WAITING");
        patient.setAdmissionDate(java.time.LocalDate.now());

        // 3. Save Patient
        com.hospital.resource.optimization.model.Patient savedPatient = patientRepository.save(patient);

        // 4. Update Doctor Workload
        bestDoctor.setCurrentWorkload(bestDoctor.getCurrentWorkload() + 1);
        doctorRepository.save(bestDoctor);

        return ResponseEntity.ok(savedPatient);
    }

    // --- ADMISSION MANAGEMENT ---

    @DeleteMapping("/admissions/{id}")
    public ResponseEntity<Void> deleteAdmission(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    // --- BED MANAGEMENT ---

    @GetMapping("/beds")
    public ResponseEntity<List<Bed>> getAllBeds() {
        return ResponseEntity.ok(bedService.getAllBeds());
    }

    // --- EQUIPMENT MANAGEMENT ---
    // REMOVED: Staff should not manage equipment directly. Admin only.

    // --- TEST MANAGEMENT ---

    @Autowired
    private com.hospital.resource.optimization.service.TestService testService;

    @GetMapping({ "/tests", "/tests/master" })
    public ResponseEntity<List<com.hospital.resource.optimization.model.Test>> getAllTests() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @PostMapping({ "/tests", "/tests/master" })
    public ResponseEntity<com.hospital.resource.optimization.model.Test> addTest(
            @RequestBody com.hospital.resource.optimization.model.Test test) {
        return ResponseEntity.ok(testService.addTest(test));
    }

    @PutMapping("/tests/{id}/toggle")
    public ResponseEntity<Void> toggleTestStatus(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/tests/complete/{assignmentId}")
    public ResponseEntity<Void> completeTest(@PathVariable Long assignmentId) {
        testService.completeTest(assignmentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tests/queue")
    public ResponseEntity<List<com.hospital.resource.optimization.model.TestAssignment>> getTestQueue() {
        return ResponseEntity.ok(testService.getActiveQueue());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<java.util.Map<String, Object>> getDashboardStats() {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<com.hospital.resource.optimization.model.Patient> allPatients = patientRepository.findAll();

        long newPatientsToday = allPatients.stream()
                .filter(p -> p.getAdmissionDate() != null && p.getAdmissionDate().isEqual(today))
                .count();

        // Use repository to fetch counts directly
        List<com.hospital.resource.optimization.model.TestAssignment> pendingList = testAssignmentRepository
                .findByStatus("PENDING");
        long pendingTests = pendingList.size();

        List<com.hospital.resource.optimization.model.TestAssignment> completedList = new java.util.ArrayList<>();
        try {
            completedList = testAssignmentRepository.findByStatus("COMPLETED");
        } catch (Exception e) {
            // Fallback
        }
        long completedTests = completedList.size();

        // --- NEW: Patient Activity (OPD + Appointments) ---
        List<java.util.Map<String, Object>> recentActivity = new java.util.ArrayList<>();

        // Add OPD Patients
        List<com.hospital.resource.optimization.model.Patient> todayOPD = allPatients.stream()
                .filter(p -> p.getAdmissionDate() != null && p.getAdmissionDate().isEqual(today))
                .collect(java.util.stream.Collectors.toList());

        for (com.hospital.resource.optimization.model.Patient p : todayOPD) {
            java.util.Map<String, Object> activity = new java.util.HashMap<>();
            activity.put("id", "OPD-" + p.getId());
            activity.put("name", p.getName());
            activity.put("type", "OPD");
            activity.put("doctor", p.getAssignedDoctor() != null ? p.getAssignedDoctor().getName() : "Unassigned");
            activity.put("time", "Admitted");
            activity.put("status", p.getStatus());
            recentActivity.add(activity);
        }

        // Add Appointments
        List<com.hospital.resource.optimization.model.Appointment> todayAppts = appointmentRepository.findByDate(today);
        long appointmentCount = todayAppts.size();

        for (com.hospital.resource.optimization.model.Appointment a : todayAppts) {
            java.util.Map<String, Object> activity = new java.util.HashMap<>();
            activity.put("id", "APT-" + a.getId());
            activity.put("name", a.getPatient() != null ? a.getPatient().getName() : "Unknown");
            activity.put("type", "Appointment");
            activity.put("doctor", a.getDoctor() != null ? a.getDoctor().getName() : "Unassigned");
            activity.put("time", a.getTime() != null ? a.getTime().toString() : "Scheduled");
            activity.put("status", a.getStatus());
            recentActivity.add(activity);
        }

        // Notifications
        List<String> notifications = new java.util.ArrayList<>();
        if (newPatientsToday > 0)
            notifications.add(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                    + ": New patient admitted.");
        if (pendingTests > 0)
            notifications.add(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                    + ": New test assigned (" + pendingTests + " pending).");
        if (appointmentCount > 0)
            notifications.add(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                    + ": New appointments (" + appointmentCount + ").");

        if (notifications.isEmpty())
            notifications.add("System: Dashboard ready");

        // Doctor Test Counts
        java.util.Map<String, Long> doctorTestCounts = pendingList.stream()
                .filter(a -> a.getDoctor() != null)
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getDoctor().getName(),
                        java.util.stream.Collectors.counting()));

        // Daily Volume (Mock)
        java.util.Map<String, Long> dailyVolume = new java.util.LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dailyVolume.put(today.minusDays(i).toString(), (long) (Math.random() * 20));
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        // Summary Counts
        response.put("opdCount", newPatientsToday);
        response.put("appointmentCount", appointmentCount);
        response.put("totalPatientsToday", newPatientsToday + appointmentCount);

        // Legacy
        response.put("newPatientsToday", newPatientsToday);

        // Tests
        response.put("pendingTests", pendingTests);
        response.put("completedTests", completedTests);

        response.put("doctorTestCounts", doctorTestCounts);
        response.put("dailyVolume", dailyVolume);
        response.put("notifications", notifications);
        response.put("recentActivity", recentActivity);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctors/{id}/stats")
    public ResponseEntity<java.util.Map<String, Object>> getDoctorStats(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(doctor -> {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    long opdCount = doctor.getCurrentWorkload();
                    long todayApptCount = appointmentRepository.findByDoctorAndDate(doctor, today).size();

                    java.util.Map<String, Object> stats = new java.util.HashMap<>();
                    stats.put("opdCount", opdCount);
                    stats.put("appointmentCount", todayApptCount);
                    stats.put("totalToday", opdCount + todayApptCount);

                    return ResponseEntity.ok(stats);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
