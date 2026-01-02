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
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private BedService bedService;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private com.hospital.resource.optimization.repository.StaffRepository staffRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // --- BED MANAGEMENT ---

    @GetMapping("/beds")
    public ResponseEntity<List<Bed>> getAllBeds() {
        return ResponseEntity.ok(bedService.getAllBeds());
    }

    @PostMapping("/beds")
    public ResponseEntity<Bed> addBed(@RequestBody Bed bed) {
        return ResponseEntity.ok(bedService.addBed(bed));
    }

    @PutMapping("/beds/{id}")
    public ResponseEntity<Bed> updateBed(@PathVariable Long id, @RequestBody Bed bed) {
        return ResponseEntity.ok(bedService.updateBed(id, bed));
    }

    @PutMapping("/beds/{id}/toggle")
    public ResponseEntity<Bed> toggleBedStatus(@PathVariable Long id) {
        return ResponseEntity.ok(bedService.toggleStatus(id));
    }

    // --- EQUIPMENT MANAGEMENT ---

    @GetMapping("/equipment")
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }

    @PostMapping("/equipment")
    public ResponseEntity<Equipment> addEquipment(@RequestBody Equipment equipment) {
        return ResponseEntity.ok(equipmentService.addEquipment(equipment));
    }

    @PutMapping("/equipment/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, equipment));
    }

    @PutMapping("/equipment/{id}/toggle")
    public ResponseEntity<Equipment> toggleEquipmentStatus(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.toggleStatus(id));
    }

    // --- STAFF MANAGEMENT ---

    @Autowired
    private com.hospital.resource.optimization.repository.UserRepository userRepository;

    @GetMapping("/staff")
    public ResponseEntity<List<com.hospital.resource.optimization.model.Staff>> getAllStaff() {
        return ResponseEntity.ok(staffRepository.findAll());
    }

    @PutMapping("/staff/{id}")
    public ResponseEntity<com.hospital.resource.optimization.model.Staff> updateStaff(@PathVariable Long id,
            @RequestBody java.util.Map<String, String> payload) {
        return staffRepository.findById(id)
                .map(staff -> {
                    // Update Staff details
                    staff.setName(payload.get("name"));
                    staff.setEmail(payload.get("username"));
                    staff.setPhoneNumber(payload.get("phoneNumber"));
                    staff.setAddress(payload.get("address"));
                    staff.setShift(payload.get("shift"));

                    // Update linked User details
                    com.hospital.resource.optimization.model.User user = staff.getUser();
                    if (user != null) {
                        user.setName(payload.get("name"));
                        user.setUsername(payload.get("username"));
                        // user.setEnabled is handled by toggle
                        userRepository.save(user);
                    }

                    return ResponseEntity.ok(staffRepository.save(staff));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/staff/{id}/toggle")
    public ResponseEntity<com.hospital.resource.optimization.model.Staff> toggleStaff(@PathVariable Long id) {
        return staffRepository.findById(id)
                .map(staff -> {
                    com.hospital.resource.optimization.model.User user = staff.getUser();
                    if (user != null) {
                        user.setEnabled(!user.isEnabled());
                        userRepository.save(user);
                    }
                    return ResponseEntity.ok(staff);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody java.util.Map<String, String> payload) {
        if (userRepository.existsByUsername(payload.get("username"))) {
            return ResponseEntity.badRequest()
                    .body(java.util.Collections.singletonMap("message", "Username already exists"));
        }

        // 1. Create User
        com.hospital.resource.optimization.model.User user = new com.hospital.resource.optimization.model.User();
        user.setName(payload.get("name"));
        user.setUsername(payload.get("username"));
        user.setPassword(passwordEncoder.encode(payload.get("password")));
        user.setRole(com.hospital.resource.optimization.model.Role.STAFF);
        user.setEnabled(true);
        com.hospital.resource.optimization.model.User savedUser = userRepository.save(user);

        // 2. Create Staff linked to User
        com.hospital.resource.optimization.model.Staff staff = new com.hospital.resource.optimization.model.Staff();
        staff.setName(payload.get("name"));
        staff.setEmail(payload.get("username"));
        staff.setPhoneNumber(payload.get("phoneNumber"));
        staff.setAddress(payload.get("address"));
        staff.setShift(payload.get("shift"));
        staff.setUser(savedUser);

        com.hospital.resource.optimization.model.Staff savedStaff = staffRepository.save(staff);

        // 3. Generate Custom ID (Simple logic: STF--{ID})
        savedStaff.setCustomId("STF--" + String.format("%03d", savedStaff.getId()));
        staffRepository.save(savedStaff);

        return ResponseEntity.ok(savedStaff);
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        return staffRepository.findById(id)
                .map(staff -> {
                    com.hospital.resource.optimization.model.User user = staff.getUser();
                    staffRepository.delete(staff);
                    if (user != null) {
                        userRepository.delete(user);
                    }
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private com.hospital.resource.optimization.service.TestService testService;

    @GetMapping("/tests")
    public ResponseEntity<List<com.hospital.resource.optimization.model.Test>> getAllTests() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @PostMapping("/tests")
    public ResponseEntity<com.hospital.resource.optimization.model.Test> addTest(
            @RequestBody com.hospital.resource.optimization.model.Test test) {
        return ResponseEntity.ok(testService.addTest(test));
    }

    @PutMapping("/tests/{id}/toggle")
    public ResponseEntity<com.hospital.resource.optimization.model.Test> toggleTestStatus(@PathVariable Long id) {
        return ResponseEntity.ok(testService.toggleStatus(id));
    }

    @PutMapping("/tests/{id}")
    public ResponseEntity<com.hospital.resource.optimization.model.Test> updateTest(
            @PathVariable Long id, @RequestBody com.hospital.resource.optimization.model.Test testDetails) {
        return ResponseEntity.ok(testService.updateTest(id, testDetails));
    }

    @DeleteMapping("/tests/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long id) {
        testService.deleteTest(id);
        return ResponseEntity.noContent().build();
    }
    // --- GLOBAL USER/ROLE MANAGEMENT (BILLING, TESTHANDLER, etc.) ---

    @GetMapping("/users")
    public ResponseEntity<List<com.hospital.resource.optimization.model.User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Reuse STAFF structure or standard USER structure for new roles
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody java.util.Map<String, String> payload) {
        if (userRepository.existsByUsername(payload.get("username"))) {
            return ResponseEntity.badRequest()
                    .body(java.util.Collections.singletonMap("message", "Username already exists"));
        }

        try {
            com.hospital.resource.optimization.model.User user = new com.hospital.resource.optimization.model.User();
            user.setName(payload.get("name"));
            user.setUsername(payload.get("username"));
            user.setPassword(passwordEncoder.encode(payload.get("password")));

            String roleStr = payload.get("role"); // BILLING, TESTHANDLER
            user.setRole(com.hospital.resource.optimization.model.Role.valueOf(roleStr));
            user.setEnabled(true);

            com.hospital.resource.optimization.model.User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("message", "Invalid Role"));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<com.hospital.resource.optimization.model.User> updateUser(@PathVariable Long id,
            @RequestBody java.util.Map<String, String> payload) {
        return userRepository.findById(id).map(user -> {
            user.setName(payload.get("name"));
            user.setUsername(payload.get("username"));
            // Password update optional? For now, skip
            if (payload.containsKey("role")) {
                try {
                    user.setRole(com.hospital.resource.optimization.model.Role.valueOf(payload.get("role")));
                } catch (Exception e) {
                }
            }
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
