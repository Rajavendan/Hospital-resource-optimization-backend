package com.hospital.resource.optimization.service.impl;

import com.hospital.resource.optimization.model.Patient;

import com.hospital.resource.optimization.model.Test;
import com.hospital.resource.optimization.repository.PatientRepository;
import com.hospital.resource.optimization.repository.TestAssignmentRepository;
import com.hospital.resource.optimization.repository.TestRepository;
import com.hospital.resource.optimization.service.TestService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private com.hospital.resource.optimization.repository.TestAssignmentRepository testAssignmentRepository; // Use new
                                                                                                             // repository

    @PostConstruct
    public void seedTests() {
        if (testRepository.count() == 0) {
            String[] commonTests = {
                    "Complete Blood Count (CBC)", "Lipid Panel", "Liver Function Test", "Kidney Function Test",
                    "Thyroid Profile",
                    "HbA1c", "Serum Electrolytes", "Urine Analysis", "Vitamin D", "Vitamin B12",
                    "X-Ray Chest", "MRI Brain", "CT Scan Head", "Ultrasound Abdomen", "ECG",
                    "Echocardiogram", "Bone Density Scan", "Mammography", "Pap Smear", "PSA Test"
            };

            for (String name : commonTests) {
                Test test = new Test();
                test.setName(name);
                test.setDepartment("General"); // Default department
                test.setCost(500);
                test.setMaxCapacity(20);
                testRepository.save(test);
            }
        }
    }

    @Override
    public List<Test> getAllTests() {
        List<Test> tests = testRepository.findAll();
        // Populate current count for today
        java.time.LocalDate today = java.time.LocalDate.now();
        for (Test test : tests) {
            long count = testAssignmentRepository.countByTest_IdAndAssignedDateBetween(test.getId(),
                    today.atStartOfDay(),
                    today.atTime(java.time.LocalTime.MAX));
            test.setCurrentCount((int) count);
        }
        return tests;
    }

    @Override
    public Test addTest(Test test) {
        if (test.getMaxCapacity() == 0) {
            test.setMaxCapacity(20);
        }
        return testRepository.save(test);
    }

    @Override
    public Test toggleStatus(Long id) {
        return testRepository.findById(id).map(test -> {
            test.setStatus("ACTIVE".equals(test.getStatus()) ? "INACTIVE" : "ACTIVE");
            return testRepository.save(test);
        }).orElse(null);
    }

    @Override
    public Test updateTest(Long id, Test testDetails) {
        return testRepository.findById(id).map(test -> {
            test.setName(testDetails.getName());
            test.setDepartment(testDetails.getDepartment());
            test.setCost(testDetails.getCost());
            test.setMaxCapacity(testDetails.getMaxCapacity());
            test.setStatus(testDetails.getStatus());
            test.setDescription(testDetails.getDescription());
            return testRepository.save(test);
        }).orElseThrow(() -> new RuntimeException("Test not found with id " + id));
    }

    @Override
    public void deleteTest(Long id) {
        testRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void assignTestsToPatient(Long patientId, Long doctorId, List<Long> testIds) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Fetch doctor entity purely for reference if needed, but ID is enough for now
        // Assuming Doctor repository/entity availability
        com.hospital.resource.optimization.model.Doctor doctor = new com.hospital.resource.optimization.model.Doctor();
        doctor.setId(doctorId);

        for (Long testId : testIds) {
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found: " + testId));

            // Create New Assignment
            com.hospital.resource.optimization.model.TestAssignment assignment = new com.hospital.resource.optimization.model.TestAssignment();
            assignment.setPatient(patient);
            assignment.setTest(test);
            assignment.setDoctor(doctor); // This requires Doctor entity to be managed
            assignment.setStatus("WAITING_FOR_PAYMENT");
            assignment.setPaymentStatus("PENDING");

            testAssignmentRepository.save(assignment);
        }
    }

    @Override
    @Transactional
    public void completeTest(Long assignmentId) {
        com.hospital.resource.optimization.model.TestAssignment assignment = testAssignmentRepository
                .findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Test assignment not found"));

        if (!"COMPLETED".equals(assignment.getStatus())) {
            assignment.setStatus("COMPLETED");
            assignment.setCompletedDate(LocalDateTime.now());
            testAssignmentRepository.save(assignment);
        }
    }

    @Override
    public List<com.hospital.resource.optimization.model.TestAssignment> getActiveQueue() {
        return testAssignmentRepository.findByStatusIn(java.util.Arrays.asList("PENDING", "WAITING_FOR_PAYMENT"));
    }
}
