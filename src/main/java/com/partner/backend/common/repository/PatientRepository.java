package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPhone(String phone);
    boolean existsByPhone(String phone);

    Optional<Patient> findByUser_Id(Long userId);

    Optional<Patient> findByPhoneAndUserIsNotNull(String phone);

    @Query("SELECT COUNT(p) > 0 FROM Patient p WHERE p.phone = :phone AND p.user IS NOT NULL")
    boolean existsRegisteredAppPatientByPhone(@Param("phone") String phone);
    Page<Patient> findAll(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Appointment a JOIN a.patient p WHERE a.doctor.id = :doctorId AND p IS NOT NULL",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Appointment a JOIN a.patient p WHERE a.doctor.id = :doctorId AND p IS NOT NULL")
    Page<Patient> findPatientsByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);
}
