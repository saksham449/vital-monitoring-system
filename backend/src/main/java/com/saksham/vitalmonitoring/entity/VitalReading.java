package com.saksham.vitalmonitoring.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vital_readings", indexes = {
        @Index(name = "idx_vital_patient_timestamp", columnList = "patient_id,timestamp")
})
public class VitalReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private int heartRate;

    @Column(nullable = false)
    private int spo2;

    @Column(nullable = false)
    private double temperature;

    @Column(nullable = false)
    private int systolic;

    @Column(nullable = false)
    private int diastolic;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected VitalReading() {}

    public VitalReading(Patient patient, int heartRate, int spo2, double temperature,
                        int systolic, int diastolic, LocalDateTime timestamp) {
        this.patient = patient;
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.temperature = temperature;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public int getHeartRate() { return heartRate; }
    public int getSpo2() { return spo2; }
    public double getTemperature() { return temperature; }
    public int getSystolic() { return systolic; }
    public int getDiastolic() { return diastolic; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
