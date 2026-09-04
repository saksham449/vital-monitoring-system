package com.saksham.vitalmonitoring.service;

import com.saksham.vitalmonitoring.dto.BedStatusResponse;
import com.saksham.vitalmonitoring.entity.Alert;
import com.saksham.vitalmonitoring.entity.Patient;
import com.saksham.vitalmonitoring.entity.VitalReading;
import com.saksham.vitalmonitoring.model.VitalData;
import com.saksham.vitalmonitoring.repository.PatientRepository;
import com.saksham.vitalmonitoring.repository.VitalReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VitalProcessingService {
    private static final Logger log = LoggerFactory.getLogger(VitalProcessingService.class);

    private final PatientRepository patientRepository;
    private final VitalReadingRepository readingRepository;
    private final CurrentVitalsService currentVitalsService;
    private final AlertService alertService;
    private final WebSocketService webSocketService;

    public VitalProcessingService(PatientRepository patientRepository,
                                  VitalReadingRepository readingRepository,
                                  CurrentVitalsService currentVitalsService,
                                  AlertService alertService,
                                  WebSocketService webSocketService) {
        this.patientRepository = patientRepository;
        this.readingRepository = readingRepository;
        this.currentVitalsService = currentVitalsService;
        this.alertService = alertService;
        this.webSocketService = webSocketService;
    }

    @Transactional
    public void process(VitalData data) {
        if (!valid(data)) {
            log.warn("Ignoring invalid vital payload for bed {}", data == null ? "unknown" : data.bedId());
            return;
        }

        Patient patient = patientRepository.findByBedId(data.bedId()).orElse(null);
        if (patient == null) {
            log.warn("Ignoring reading for unknown bed {}", data.bedId());
            return;
        }

        LocalDateTime timestamp = data.timestamp() == null ? LocalDateTime.now() : data.timestamp();
        currentVitalsService.update(data);
        readingRepository.save(new VitalReading(patient, data.heartRate(), data.spo2(), data.temperature(),
                data.systolic(), data.diastolic(), timestamp));

        evaluateHeartRate(patient, data.heartRate(), timestamp);
        evaluateSpo2(patient, data.spo2(), timestamp);
        evaluateTemperature(patient, data.temperature(), timestamp);
        evaluateBloodPressure(patient, data.systolic(), data.diastolic(), timestamp);

        webSocketService.publishBedStatus(toBedStatus(patient, data));
    }

    private boolean valid(VitalData d) {
        return d != null && d.bedId() != null && !d.bedId().isBlank()
                && d.heartRate() != null && d.spo2() != null && d.temperature() != null
                && d.systolic() != null && d.diastolic() != null;
    }

    private void evaluateHeartRate(Patient p, int value, LocalDateTime now) {
        if (value > 120) alertService.upsertActiveAlert(p, Alert.VitalType.HEART_RATE, value, ">120", Alert.Severity.CRITICAL, now);
        else if (value < 50) alertService.upsertActiveAlert(p, Alert.VitalType.HEART_RATE, value, "<50", Alert.Severity.CRITICAL, now);
        else alertService.resolve(p, Alert.VitalType.HEART_RATE, now);
    }

    private void evaluateSpo2(Patient p, int value, LocalDateTime now) {
        if (value < 92) alertService.upsertActiveAlert(p, Alert.VitalType.SPO2, value, "<92", Alert.Severity.CRITICAL, now);
        else if (value <= 94) alertService.upsertActiveAlert(p, Alert.VitalType.SPO2, value, "92-94", Alert.Severity.WARNING, now);
        else alertService.resolve(p, Alert.VitalType.SPO2, now);
    }

    private void evaluateTemperature(Patient p, double value, LocalDateTime now) {
        if (value > 38.0) alertService.upsertActiveAlert(p, Alert.VitalType.TEMPERATURE, value, ">38.0", Alert.Severity.WARNING, now);
        else alertService.resolve(p, Alert.VitalType.TEMPERATURE, now);
    }

    private void evaluateBloodPressure(Patient p, int systolic, int diastolic, LocalDateTime now) {
        if (systolic > 140 || diastolic > 90) {
            alertService.upsertActiveAlert(p, Alert.VitalType.BLOOD_PRESSURE,
                    systolic, "Systolic >140 OR diastolic >90", Alert.Severity.WARNING, now);
        } else {
            alertService.resolve(p, Alert.VitalType.BLOOD_PRESSURE, now);
        }
    }

    public String calculateStatus(VitalData data, Collection<Alert> activeAlerts) {
        boolean critical = activeAlerts.stream().anyMatch(a -> a.getStatus() == Alert.Status.ACTIVE && a.getSeverity() == Alert.Severity.CRITICAL);
        boolean warning = activeAlerts.stream().anyMatch(a -> a.getStatus() == Alert.Status.ACTIVE && a.getSeverity() == Alert.Severity.WARNING);
        if (critical) return "CRITICAL";
        if (warning) return "WARNING";
        return "NORMAL";
    }

    private BedStatusResponse toBedStatus(Patient patient, VitalData data) {
        // Read the just-evaluated active alerts through a small repository query indirectly via AlertService.
        // The status can be derived from the latest reading using the same demo rules, avoiding extra DB coupling.
        String status = "NORMAL";
        if (data.heartRate() > 120 || data.heartRate() < 50 || data.spo2() < 92) status = "CRITICAL";
        else if (data.spo2() <= 94 || data.temperature() > 38.0 || data.systolic() > 140 || data.diastolic() > 90) status = "WARNING";
        return new BedStatusResponse(patient.getBedId(), patient.getId(), patient.getName(), patient.getAge(),
                data.heartRate(), data.spo2(), data.temperature(), data.systolic(), data.diastolic(), status);
    }
}
