package com.hospital.resource.optimization.scheduler;

import com.hospital.resource.optimization.model.Alert;
import com.hospital.resource.optimization.repository.*;
import com.hospital.resource.optimization.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SchedulerService {

    private final BedRepository bedRepository;
    private final DoctorRepository doctorRepository;
    private final AlertRepository alertRepository;
    private final NotificationService notificationService;

    public SchedulerService(BedRepository bedRepository, DoctorRepository doctorRepository,
            AlertRepository alertRepository, NotificationService notificationService) {
        this.bedRepository = bedRepository;
        this.doctorRepository = doctorRepository;
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorResources() {
        System.out.println("Scheduler: Monitoring resources...");

        // Check Bed Availability
        long availableBeds = bedRepository.countByStatus("AVAILABLE");
        long totalBeds = bedRepository.count();
        if (totalBeds > 0 && ((double) availableBeds / totalBeds) < 0.2) {
            createAlert("CRITICAL", "Severe Bed Shortage: Only " + availableBeds + " beds left.");
            notificationService.sendEmail("admin@hospital.com", "Bed Shortage", "Action needed immediately.");
        }

        // Check Doctor Workload
        doctorRepository.findAll().forEach(doc -> {
            if (doc.getCurrentWorkload() >= doc.getMaxLoad()) {
                createAlert("STAFFING", "Doctor " + doc.getName() + " is at max capacity.");
            }
        });
    }

    private void createAlert(String type, String message) {
        Alert alert = new Alert();
        alert.setType(type);
        alert.setMessage(message);
        alert.setTimestamp(LocalDateTime.now());
        alert.setResolved(false);
        alertRepository.save(alert);
    }
}
