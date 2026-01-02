package com.hospital.resource.optimization.service;

import com.hospital.resource.optimization.model.Equipment;
import java.util.List;

public interface EquipmentService {
    List<Equipment> getAllEquipment();

    Equipment addEquipment(Equipment equipment);

    Equipment updateEquipment(Long id, Equipment equipmentDetails);

    Equipment getEquipmentById(Long id);

    Equipment toggleStatus(Long id); // Toggles between AVAILABLE/IN_USE/MAINTENANCE
}
