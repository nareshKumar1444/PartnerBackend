package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Availability;
import com.partner.backend.common.entity.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByDoctorId(Long doctorId);
    Optional<Availability> findByDoctorIdAndDayOfWeek(Long doctorId, WeekDay day);
    void deleteByDoctorId(Long doctorId);
}
