package com.partner.backend.common.repository;

import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByProviderIdAndProviderType(Long providerId, ProviderType providerType);
}
