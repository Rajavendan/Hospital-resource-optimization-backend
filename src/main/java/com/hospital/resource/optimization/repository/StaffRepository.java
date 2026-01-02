package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUserId(Long userId);

    Optional<Staff> findByEmail(String email);
}
