package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByWardAndStatus(String ward, String status);

    long countByStatus(String status);

    List<Bed> findByStatus(String status);
}
