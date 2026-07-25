package com.partner.backend.common.repository;

import com.partner.backend.common.entity.PatientHealthReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientHealthReadingRepository extends JpaRepository<PatientHealthReading, Long> {

    List<PatientHealthReading> findByPatient_IdOrderByReadingDateDescCreatedAtDesc(Long patientId);
}
