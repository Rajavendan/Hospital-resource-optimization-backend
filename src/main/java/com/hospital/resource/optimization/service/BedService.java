package com.hospital.resource.optimization.service;

import com.hospital.resource.optimization.model.Bed;
import java.util.List;

public interface BedService {
    List<Bed> getAllBeds();

    Bed addBed(Bed bed);

    Bed updateBed(Long id, Bed bedDetails);

    Bed toggleStatus(Long id);

    Bed assignBed(String ward);

    void releaseBed(Long bedId);

    Bed getBedById(Long id);
}
