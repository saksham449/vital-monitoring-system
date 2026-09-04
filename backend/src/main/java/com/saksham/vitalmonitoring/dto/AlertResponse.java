package com.saksham.vitalmonitoring.dto;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        String bedId,
        String patientName,
        String vitalType,
        double value,
        String threshold,
        String severity,
        String status,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {}
