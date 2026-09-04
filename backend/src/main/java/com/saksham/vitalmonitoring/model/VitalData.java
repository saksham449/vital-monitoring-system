package com.saksham.vitalmonitoring.model;

import java.time.LocalDateTime;

public record VitalData(
        String bedId,
        Integer heartRate,
        Integer spo2,
        Double temperature,
        Integer systolic,
        Integer diastolic,
        LocalDateTime timestamp
) {}
