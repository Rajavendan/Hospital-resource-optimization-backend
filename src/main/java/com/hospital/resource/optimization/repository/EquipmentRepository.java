package com.hospital.resource.optimization.repository;

import com.hospital.resource.optimization.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByType(String type);

    List<Equipment> findByStatus(String status);
}
