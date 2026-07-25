package com.partner.backend.common.repository;

import com.partner.backend.common.entity.LabAppointment;
import com.partner.backend.common.entity.LabAppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LabAppointmentRepository extends JpaRepository<LabAppointment, Long> {
    Page<LabAppointment> findByLabId(Long labId, Pageable pageable);
    long countByLabIdAndStatus(Long labId, LabAppointmentStatus status);
    long countByLabId(Long labId);

    @Query("SELECT SUM(a.test.discountedPrice) FROM LabAppointment a WHERE a.lab.id = :labId AND a.status = 'COMPLETED'")
    Double sumCompletedAmountByLabId(Long labId);

    @Query("""
            SELECT COALESCE(SUM(a.test.discountedPrice), 0) FROM LabAppointment a
            WHERE a.lab.id = :labId AND a.status = 'COMPLETED'
            AND a.createdAt >= :start AND a.createdAt < :end
            """)
    BigDecimal sumCompletedAmountByLabBetween(Long labId, LocalDateTime start, LocalDateTime end);

    List<LabAppointment> findByPatient_IdOrderByScheduledDateDesc(Long patientId);

    long countByPatient_Id(Long patientId);

    List<LabAppointment> findByLabIdAndScheduledDate(Long labId, java.time.LocalDate scheduledDate);

    @Query("""
            SELECT a FROM LabAppointment a
            WHERE a.scheduledDate < :cutoffDate
            AND a.status IN :statuses
            """)
    List<LabAppointment> findExpiredPendingOrConfirmed(
            @Param("cutoffDate") LocalDate cutoffDate,
            @Param("statuses") List<LabAppointmentStatus> statuses);
}
