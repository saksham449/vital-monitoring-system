package com.saksham.vitalmonitoring.repository;

import com.saksham.vitalmonitoring.entity.VitalReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VitalReadingRepository extends JpaRepository<VitalReading, Long> {}
