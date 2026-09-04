package com.saksham.vitalmonitoring.service;

import com.saksham.vitalmonitoring.dto.AlertResponse;
import com.saksham.vitalmonitoring.entity.Alert;
import com.saksham.vitalmonitoring.entity.Patient;
import com.saksham.vitalmonitoring.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final WebSocketService webSocketService;

    public AlertService(AlertRepository alertRepository, WebSocketService webSocketService) {
        this.alertRepository = alertRepository;
        this.webSocketService = webSocketService;
    }

    @Transactional
    public Alert upsertActiveAlert(Patient patient, Alert.VitalType vitalType, double value,
                                   String threshold, Alert.Severity severity, LocalDateTime now) {
        Alert alert = alertRepository
                .findByPatientAndVitalTypeAndStatus(patient, vitalType, Alert.Status.ACTIVE)
                .orElseGet(() -> new Alert(patient, vitalType, value, threshold, severity, now));

        boolean isNew = alert.getId() == null;
        alert.update(value, threshold, severity);
        alert = alertRepository.save(alert);

        // Publish both new alerts and severity/value changes so the live UI stays current.
        if (isNew || !alert.getStatus().equals(Alert.Status.RESOLVED)) {
            webSocketService.publishAlert(toResponse(alert));
        }
        return alert;
    }

    @Transactional
    public void resolve(Patient patient, Alert.VitalType vitalType, LocalDateTime now) {
        alertRepository.findByPatientAndVitalTypeAndStatus(patient, vitalType, Alert.Status.ACTIVE)
                .ifPresent(alert -> {
                    alert.resolve(now);
                    alertRepository.save(alert);
                    webSocketService.publishAlert(toResponse(alert));
                });
    }

    public List<AlertResponse> all() {
        return alertRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<AlertResponse> active() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(Alert.Status.ACTIVE).stream().map(this::toResponse).toList();
    }

    public List<AlertResponse> byBed(String bedId) {
        return alertRepository.findByPatientBedIdOrderByCreatedAtDesc(bedId).stream().map(this::toResponse).toList();
    }

    public AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(), alert.getBedId(), alert.getPatient().getName(),
                alert.getVitalType().name(), alert.getValue(), alert.getThreshold(),
                alert.getSeverity().name(), alert.getStatus().name(),
                alert.getCreatedAt(), alert.getResolvedAt());
    }
}
