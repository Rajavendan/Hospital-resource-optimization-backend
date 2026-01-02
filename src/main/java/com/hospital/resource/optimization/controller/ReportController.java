package com.hospital.resource.optimization.controller;

import com.hospital.resource.optimization.repository.TestAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private TestAssignmentRepository testAssignmentRepository;

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    @GetMapping("/download/{assignmentId}")
    public ResponseEntity<?> downloadReport(@PathVariable Long assignmentId, Authentication authentication) {
        return testAssignmentRepository.findById(assignmentId).map(assignment -> {
            try {
                // Basic Security Check (Improvement: Verify User relationship)
                // For now, assuming authenticated users have access based on roles handled by
                // frontend fetching logic
                // Ideally: if (no access) return 403;

                if (assignment.getReportPath() == null) {
                    return ResponseEntity.notFound().build();
                }

                Path filePath = this.fileStorageLocation.resolve(assignment.getReportPath()).normalize();
                Resource resource = new UrlResource(filePath.toUri());

                if (resource.exists()) {
                    String contentType = "application/octet-stream"; // Default
                    try {
                        contentType = java.nio.file.Files.probeContentType(filePath);
                    } catch (Exception e) {
                        // ignore
                    }

                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + resource.getFilename() + "\"")
                            .body(resource);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception ex) {
                return ResponseEntity.internalServerError().<Resource>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
