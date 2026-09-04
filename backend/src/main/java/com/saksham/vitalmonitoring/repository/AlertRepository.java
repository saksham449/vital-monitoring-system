package com.saksham.vitalmonitoring.repository;

import com.saksham.vitalmonitoring.entity.Alert;
import com.saksham.vitalmonitoring.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Optional<Alert> findByPatientAndVitalTypeAndStatus(Patient patient, Alert.VitalType vitalType, Alert.Status status);
    List<Alert> findByStatusOrderByCreatedAtDesc(Alert.Status status);
    List<Alert> findAllByOrderByCreatedAtDesc();
    List<Alert> findByPatientBedIdOrderByCreatedAtDesc(String bedId);
}
