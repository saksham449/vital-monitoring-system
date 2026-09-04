package com.saksham.vitalmonitoring.controller;

import com.saksham.vitalmonitoring.dto.BedStatusResponse;
import com.saksham.vitalmonitoring.entity.Patient;
import com.saksham.vitalmonitoring.model.VitalData;
import com.saksham.vitalmonitoring.repository.PatientRepository;
import com.saksham.vitalmonitoring.service.CurrentVitalsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beds")
public class BedController {
    private final PatientRepository patientRepository;
    private final CurrentVitalsService currentVitalsService;

    public BedController(PatientRepository patientRepository, CurrentVitalsService currentVitalsService) {
        this.patientRepository = patientRepository;
        this.currentVitalsService = currentVitalsService;
    }

    @GetMapping
    public List<BedStatusResponse> getBeds() {
        return patientRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{bedId}")
    public ResponseEntity<BedStatusResponse> getBed(@PathVariable String bedId) {
        return patientRepository.findByBedId(bedId)
                .map(patient -> ResponseEntity.ok(toResponse(patient)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private BedStatusResponse toResponse(Patient patient) {
        VitalData data = currentVitalsService.get(patient.getBedId());
        if (data == null) {
            return new BedStatusResponse(patient.getBedId(), patient.getId(), patient.getName(), patient.getAge(),
                    null, null, null, null, null, "NORMAL");
        }
        String status = "NORMAL";
        if (data.heartRate() > 120 || data.heartRate() < 50 || data.spo2() < 92) status = "CRITICAL";
        else if (data.spo2() <= 94 || data.temperature() > 38.0 || data.systolic() > 140 || data.diastolic() > 90) status = "WARNING";
        return new BedStatusResponse(patient.getBedId(), patient.getId(), patient.getName(), patient.getAge(),
                data.heartRate(), data.spo2(), data.temperature(), data.systolic(), data.diastolic(), status);
    }
}
