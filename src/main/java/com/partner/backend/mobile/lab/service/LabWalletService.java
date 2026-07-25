package com.partner.backend.mobile.lab.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.lab.dto.LabWalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabWalletService {

    private final WalletRepository walletRepository;
    private final EarningRepository earningRepository;

    public LabWalletResponse getWallet(Long labId) {
        Wallet wallet = walletRepository
                .findByProviderIdAndProviderType(labId, ProviderType.LAB)
                .orElseGet(() -> Wallet.builder()
                        .providerId(labId)
                        .providerType(ProviderType.LAB)
                        .balance(BigDecimal.ZERO)
                        .build());

        BigDecimal totalEarnings = earningRepository.sumGrossByProvider(labId, ProviderType.LAB);
        if (totalEarnings == null) totalEarnings = BigDecimal.ZERO;

        return LabWalletResponse.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .totalEarnings(totalEarnings)
                .build();
    }
}
