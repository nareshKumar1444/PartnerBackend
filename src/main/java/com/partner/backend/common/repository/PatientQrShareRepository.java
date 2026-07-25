package com.partner.backend.common.repository;

import com.partner.backend.common.entity.PatientQrShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientQrShareRepository extends JpaRepository<PatientQrShare, Long> {
    Optional<PatientQrShare> findByAccessCode(String accessCode);

    List<PatientQrShare> findTop10ByPatient_IdOrderByCreatedAtDesc(Long patientId);
}
