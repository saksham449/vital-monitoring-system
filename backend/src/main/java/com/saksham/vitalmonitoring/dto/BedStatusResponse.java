package com.saksham.vitalmonitoring.dto;

public record BedStatusResponse(
        String bedId,
        Long patientId,
        String patientName,
        int age,
        Integer heartRate,
        Integer spo2,
        Double temperature,
        Integer systolic,
        Integer diastolic,
        String status
) {}
