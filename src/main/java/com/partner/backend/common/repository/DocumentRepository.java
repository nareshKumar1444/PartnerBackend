package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Document;
import com.partner.backend.common.entity.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByProviderIdAndProviderType(Long providerId, ProviderType type);
}
