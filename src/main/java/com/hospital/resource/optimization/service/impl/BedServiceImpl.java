package com.hospital.resource.optimization.service.impl;

import com.hospital.resource.optimization.model.Bed;
import com.hospital.resource.optimization.repository.BedRepository;
import com.hospital.resource.optimization.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class BedServiceImpl implements BedService {

    @Autowired
    private BedRepository bedRepository;

    @PostConstruct
    public void initBeds() {
        if (bedRepository.count() == 0) {
            createBeds("ICU", 10);
            createBeds("GENERAL", 10);
            createBeds("EMERGENCY", 10);
        }
    }

    private void createBeds(String ward, int count) {
        IntStream.rangeClosed(1, count).forEach(i -> {
            Bed bed = new Bed();
            bed.setWard(ward);
            bed.setBedNumber(ward.substring(0, 3) + "-" + String.format("%03d", i));
            bed.setStatus("AVAILABLE");
            bedRepository.save(bed);
        });
    }

    @Override
    public List<Bed> getAllBeds() {
        return bedRepository.findAll();
    }

    @Override
    public Bed addBed(Bed bed) {
        bed.setStatus("AVAILABLE");
        return bedRepository.save(bed);
    }

    @Override
    public Bed updateBed(Long id, Bed bedDetails) {
        Bed bed = bedRepository.findById(id).orElseThrow(() -> new RuntimeException("Bed not found"));
        bed.setWard(bedDetails.getWard());
        bed.setBedNumber(bedDetails.getBedNumber());
        return bedRepository.save(bed);
    }

    @Override
    public Bed toggleStatus(Long id) {
        Bed bed = bedRepository.findById(id).orElseThrow(() -> new RuntimeException("Bed not found"));
        if ("AVAILABLE".equals(bed.getStatus())) {
            bed.setStatus("UNAVAILABLE");
        } else if ("UNAVAILABLE".equals(bed.getStatus())) {
            bed.setStatus("AVAILABLE");
        }
        return bedRepository.save(bed);
    }

    @Override
    @Transactional
    public Bed assignBed(String ward) {
        List<Bed> availableBeds = bedRepository.findByWardAndStatus(ward, "AVAILABLE");
        if (availableBeds.isEmpty()) {
            throw new RuntimeException("No beds available in " + ward);
        }
        Bed bed = availableBeds.get(0);
        bed.setStatus("OCCUPIED");
        return bedRepository.save(bed);
    }

    @Override
    @Transactional
    public void releaseBed(Long bedId) {
        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new RuntimeException("Bed not found"));
        // Set to COOLING per Feature 2 requirements
        bed.setStatus("COOLING");
        bed.setLastDischargedAt(java.time.LocalDateTime.now());
        bed.setPatient(null);
        bedRepository.save(bed);
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000) // Check every minute
    @Transactional
    public void cleanUpCoolingBeds() {
        List<Bed> coolingBeds = bedRepository.findByStatus("COOLING");
        java.time.LocalDateTime twelveHoursAgo = java.time.LocalDateTime.now().minusHours(12);

        for (Bed bed : coolingBeds) {
            if (bed.getLastDischargedAt() != null && bed.getLastDischargedAt().isBefore(twelveHoursAgo)) {
                bed.setStatus("AVAILABLE");
                bed.setLastDischargedAt(null);
                bedRepository.save(bed);
                // System.out.println("Bed " + bed.getBedNumber() + " is now AVAILABLE after
                // cooling period.");
            }
        }
    }

    @Override
    public Bed getBedById(Long id) {
        return bedRepository.findById(id).orElseThrow(() -> new RuntimeException("Bed not found"));
    }
}
