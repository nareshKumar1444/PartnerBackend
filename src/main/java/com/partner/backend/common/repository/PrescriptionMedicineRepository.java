package com.partner.backend.common.repository;

import com.partner.backend.common.entity.PrescriptionMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionMedicineRepository extends JpaRepository<PrescriptionMedicine, Long> {
    List<PrescriptionMedicine> findByConsultationId(Long consultationId);
    void deleteByConsultationId(Long consultationId);
}
