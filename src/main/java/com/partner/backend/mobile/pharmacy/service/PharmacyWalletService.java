package com.partner.backend.mobile.pharmacy.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.pharmacy.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PharmacyWalletService {

    private final WalletRepository walletRepository;
    private final EarningRepository earningRepository;

    public WalletResponse getWallet(Long pharmacyId) {
        Wallet wallet = walletRepository
                .findByProviderIdAndProviderType(pharmacyId, ProviderType.PHARMACY)
                .orElseGet(() -> Wallet.builder()
                        .providerId(pharmacyId)
                        .providerType(ProviderType.PHARMACY)
                        .balance(BigDecimal.ZERO)
                        .build());

        BigDecimal totalEarnings = earningRepository.sumGrossByProvider(pharmacyId, ProviderType.PHARMACY);
        if (totalEarnings == null) totalEarnings = BigDecimal.ZERO;

        return WalletResponse.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .totalEarnings(totalEarnings)
                .build();
    }
}
