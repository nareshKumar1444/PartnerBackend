package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Optional<Consultation> findByAppointmentId(Long appointmentId);

    @Query("SELECT c FROM Consultation c WHERE c.appointment.doctor.id = :doctorId")
    Page<Consultation> findByDoctorId(Long doctorId, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Consultation c "
            + "JOIN FETCH c.appointment a "
            + "JOIN FETCH a.doctor "
            + "LEFT JOIN FETCH c.medicines "
            + "WHERE a.patient.id = :patientId "
            + "ORDER BY c.createdAt DESC")
    List<Consultation> findDistinctByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.appointment.patient.id = :patientId")
    long countByPatientId(@Param("patientId") Long patientId);
}
