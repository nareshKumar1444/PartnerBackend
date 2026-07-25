package com.partner.backend.common.repository;

import com.partner.backend.common.entity.LabTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    Page<LabTest> findByLabId(Long labId, Pageable pageable);
    long countByLabId(Long labId);

    @Query("SELECT t FROM LabTest t WHERE t.lab.id = :labId AND " +
           "(LOWER(t.testName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<LabTest> searchByLabId(Long labId, String query, Pageable pageable);
}
