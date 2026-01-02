package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.TestAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestAssignmentRepository extends JpaRepository<TestAssignment, Long> {
    List<TestAssignment> findByStatus(String status);

    List<TestAssignment> findByStatusIn(List<String> statuses);

    List<TestAssignment> findByPatientId(Long patientId);

    List<TestAssignment> findByDoctorId(Long doctorId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(ta) FROM TestAssignment ta WHERE ta.test.id = :testId AND ta.assignedDate BETWEEN :startOfDay AND :endOfDay")
    long countByTest_IdAndAssignedDateBetween(@org.springframework.data.repository.query.Param("testId") Long testId,
            @org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay,
            @org.springframework.data.repository.query.Param("endOfDay") java.time.LocalDateTime endOfDay);
}
