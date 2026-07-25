package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Earning;
import com.partner.backend.common.entity.ProviderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EarningRepository extends JpaRepository<Earning, Long> {
    List<Earning> findByProviderIdAndProviderType(Long providerId, ProviderType type);
    Page<Earning> findByProviderType(ProviderType type, Pageable pageable);

    @Query("SELECT SUM(e.grossAmount) FROM Earning e")
    BigDecimal sumTotalGross();

    @Query("SELECT SUM(e.commissionAmount) FROM Earning e")
    BigDecimal sumTotalCommission();

    @Query("SELECT e.month, e.year, SUM(e.grossAmount), SUM(e.commissionAmount) FROM Earning e GROUP BY e.year, e.month ORDER BY e.year DESC, e.month DESC")
    List<Object[]> monthlyBreakdown();

    @Query("SELECT e.providerId, e.providerType, SUM(e.grossAmount), SUM(e.netAmount) FROM Earning e GROUP BY e.providerId, e.providerType")
    List<Object[]> perProviderSummary();

    @Query("SELECT SUM(e.grossAmount) FROM Earning e WHERE e.providerId = :providerId AND e.providerType = :type")
    BigDecimal sumGrossByProvider(Long providerId, ProviderType type);

    @Query("SELECT SUM(e.commissionAmount) FROM Earning e WHERE e.providerId = :providerId AND e.providerType = :type")
    BigDecimal sumCommissionByProvider(Long providerId, ProviderType type);

    @Query("""
            SELECT SUM(e.grossAmount) FROM Earning e
            WHERE e.providerId = :providerId AND e.providerType = :type
            AND e.month = :month AND e.year = :year
            """)
    BigDecimal sumGrossByProviderAndMonth(Long providerId, ProviderType type, int month, int year);
}
