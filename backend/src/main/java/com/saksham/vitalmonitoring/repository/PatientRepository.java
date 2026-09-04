package com.saksham.vitalmonitoring.repository;

import com.saksham.vitalmonitoring.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByBedId(String bedId);
}
