package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Appointment;
import com.partner.backend.common.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);
    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);
    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    long countByStatus(AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'IN_PROGRESS'")
    long countActiveConsultations();

    @Query("SELECT SUM(a.fee) FROM Appointment a WHERE a.status = 'COMPLETED' AND a.doctor.id = :doctorId")
    Double sumFeeByDoctorId(Long doctorId);

    @Query("""
            SELECT COALESCE(SUM(a.fee), 0) FROM Appointment a
            WHERE a.status = 'COMPLETED' AND a.doctor.id = :doctorId
            AND a.date >= :startDate AND a.date < :endDate
            """)
    BigDecimal sumCompletedFeesByDoctorBetween(Long doctorId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.patient IS NOT NULL")
    long countDistinctPatientsByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("""
            SELECT a FROM Appointment a
            JOIN FETCH a.doctor
            LEFT JOIN FETCH a.patient
            WHERE a.status = :status
            """)
    List<Appointment> findByStatusWithRelations(AppointmentStatus status);

    List<Appointment> findByPatient_IdOrderByDateDesc(Long patientId);

    boolean existsByDoctor_IdAndDateAndTimeSlotAndStatusNot(
            Long doctorId,
            LocalDate date,
            String timeSlot,
            AppointmentStatus excludedStatus);

    boolean existsByDoctor_IdAndDateAndTimeSlot(Long doctorId, LocalDate date, String timeSlot);
}
