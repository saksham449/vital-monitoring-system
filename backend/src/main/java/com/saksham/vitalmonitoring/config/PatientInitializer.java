package com.saksham.vitalmonitoring.config;

import com.saksham.vitalmonitoring.entity.Patient;
import com.saksham.vitalmonitoring.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PatientInitializer {
    @Bean
    CommandLineRunner seedPatients(PatientRepository repository) {
        return args -> {
            createIfMissing(repository, "Patient A", 54, "BED-01");
            createIfMissing(repository, "Patient B", 62, "BED-02");
            createIfMissing(repository, "Patient C", 47, "BED-03");
            createIfMissing(repository, "Patient D", 71, "BED-04");
        };
    }

    private void createIfMissing(PatientRepository repository, String name, int age, String bedId) {
        if (repository.findByBedId(bedId).isEmpty()) {
            repository.save(new Patient(name, age, bedId));
        }
    }
}
