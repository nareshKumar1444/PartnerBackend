package com.partner.backend.common.repository;

import com.partner.backend.common.entity.LabAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabAvailabilityRepository extends JpaRepository<LabAvailability, Long> {
    List<LabAvailability> findByLabId(Long labId);

    void deleteByLabId(Long labId);
}
