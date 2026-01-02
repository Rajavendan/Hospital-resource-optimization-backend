package com.hospital.resource.optimization.service;

import com.hospital.resource.optimization.model.Test;
import java.util.List;

public interface TestService {
    List<Test> getAllTests();

    Test addTest(Test test);

    Test toggleStatus(Long id);

    Test updateTest(Long id, Test test);

    void deleteTest(Long id);

    void assignTestsToPatient(Long patientId, Long doctorId, List<Long> testIds);

    void completeTest(Long mappingId);

    List<com.hospital.resource.optimization.model.TestAssignment> getActiveQueue();
}
