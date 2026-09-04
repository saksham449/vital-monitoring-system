package com.saksham.vitalmonitoring.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_patient_vital_status", columnList = "patient_id,vital_type,status")
})
public class Alert {
    public enum VitalType { HEART_RATE, SPO2, TEMPERATURE, BLOOD_PRESSURE }
    public enum Severity { WARNING, CRITICAL }
    public enum Status { ACTIVE, RESOLVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String bedId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vital_type", nullable = false)
    private VitalType vitalType;

    @Column(nullable = false)
    private double value;

    @Column(nullable = false)
    private String threshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    protected Alert() {}

    public Alert(Patient patient, VitalType vitalType, double value, String threshold,
                 Severity severity, LocalDateTime createdAt) {
        this.patient = patient;
        this.bedId = patient.getBedId();
        this.vitalType = vitalType;
        this.value = value;
        this.threshold = threshold;
        this.severity = severity;
        this.status = Status.ACTIVE;
        this.createdAt = createdAt;
    }

    public void update(double value, String threshold, Severity severity) {
        this.value = value;
        this.threshold = threshold;
        this.severity = severity;
    }

    public void resolve(LocalDateTime time) {
        this.status = Status.RESOLVED;
        this.resolvedAt = time;
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public String getBedId() { return bedId; }
    public VitalType getVitalType() { return vitalType; }
    public double getValue() { return value; }
    public String getThreshold() { return threshold; }
    public Severity getSeverity() { return severity; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
