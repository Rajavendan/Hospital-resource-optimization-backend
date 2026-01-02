package com.hospital.resource.optimization.service.impl;

import com.hospital.resource.optimization.model.Equipment;
import com.hospital.resource.optimization.repository.EquipmentRepository;
import com.hospital.resource.optimization.service.EquipmentService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @PostConstruct
    public void seedEquipment() {
        if (equipmentRepository.count() == 0) {
            // Seed 20 items
            IntStream.rangeClosed(1, 5).forEach(i -> createEquipment("Ventilator " + i, "Critical Care"));
            IntStream.rangeClosed(1, 5).forEach(i -> createEquipment("MRI Machine " + i, "Imaging"));
            IntStream.rangeClosed(1, 5).forEach(i -> createEquipment("ECG Monitor " + i, "Monitoring"));
            IntStream.rangeClosed(1, 5).forEach(i -> createEquipment("Wheelchair " + i, "Transport"));
        }
    }

    private void createEquipment(String name, String type) {
        Equipment eq = new Equipment();
        eq.setName(name);
        eq.setType(type);
        eq.setStatus("AVAILABLE");
        eq.setHandlerName("Unassigned");
        equipmentRepository.save(eq);
    }

    @Override
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    public Equipment addEquipment(Equipment equipment) {
        if (equipment.getStatus() == null) {
            equipment.setStatus("AVAILABLE");
        }
        return equipmentRepository.save(equipment);
    }

    @Override
    public Equipment updateEquipment(Long id, Equipment equipmentDetails) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        equipment.setName(equipmentDetails.getName());
        equipment.setType(equipmentDetails.getType());
        equipment.setHandlerName(equipmentDetails.getHandlerName());
        equipment.setStatus(equipmentDetails.getStatus());
        return equipmentRepository.save(equipment);
    }

    @Override
    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Equipment not found"));
    }

    @Override
    public Equipment toggleStatus(Long id) {
        Equipment equipment = getEquipmentById(id);
        // Cycle status: AVAILABLE -> IN_USE -> MAINTENANCE -> AVAILABLE
        switch (equipment.getStatus()) {
            case "AVAILABLE":
                equipment.setStatus("IN_USE");
                break;
            case "IN_USE":
                equipment.setStatus("MAINTENANCE");
                break;
            case "MAINTENANCE":
                equipment.setStatus("AVAILABLE");
                break;
            default:
                equipment.setStatus("AVAILABLE");
        }
        return equipmentRepository.save(equipment);
    }
}
