package com.partner.backend.common.repository;

import com.partner.backend.common.entity.PharmaReward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PharmaRewardRepository extends JpaRepository<PharmaReward, Long> {
    Page<PharmaReward> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable pageable);

    @Query("SELECT SUM(r.amount) FROM PharmaReward r WHERE r.doctor.id = :doctorId")
    BigDecimal sumByDoctorId(Long doctorId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PharmaReward r WHERE r.doctor.id = :doctorId")
    void deleteByDoctorId(@Param("doctorId") Long doctorId);
}
