package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Notification;
import com.partner.backend.common.entity.ProviderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByProviderIdAndProviderTypeOrderByCreatedAtDesc(
            Long providerId, ProviderType type, Pageable pageable);
    long countByProviderIdAndProviderTypeAndReadFalse(Long providerId, ProviderType type);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.providerId = :providerId AND n.providerType = :type")
    void deleteByProviderIdAndProviderType(
            @Param("providerId") Long providerId,
            @Param("type") ProviderType type);
}
