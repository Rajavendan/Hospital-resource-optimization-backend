package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.model.TestAssignment;
import com.hospital.resource.optimization.repository.TestAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/testhandler")
@PreAuthorize("hasRole('TESTHANDLER')")
public class TestHandlerController {

    @Autowired
    private TestAssignmentRepository testAssignmentRepository;

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public TestHandlerController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TestAssignment>> getMyTasks() {
        // TestHandler sees tasks that are PAID (Status: PENDING)
        // They pick them up and complete them.
        return ResponseEntity.ok(testAssignmentRepository.findByStatus("PENDING"));
    }

    @PostMapping("/{id}/complete")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> completeTest(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return testAssignmentRepository.findById(id).map(assignment -> {
            try {
                // Ensure unique filename
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                assignment.setReportPath(fileName);
                assignment.setStatus("COMPLETED");
                assignment.setCompletedDate(LocalDateTime.now());

                testAssignmentRepository.save(assignment);
                return ResponseEntity.ok().body("{\"message\": \"Test completed and report uploaded successfully\"}");
            } catch (IOException ex) {
                return ResponseEntity.internalServerError().body("Could not upload file: " + ex.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
