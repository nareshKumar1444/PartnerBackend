package com.partner.backend.patient.service.portal;

import com.partner.backend.common.entity.Notification;
import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.exception.UnauthorizedException;
import com.partner.backend.common.repository.NotificationRepository;
import com.partner.backend.patient.dto.portal.PatientNotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<PatientNotificationResponse> list(Long patientId, Pageable pageable) {
        return notificationRepository
                .findByProviderIdAndProviderTypeOrderByCreatedAtDesc(patientId, ProviderType.PATIENT, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void markRead(Long patientId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!n.getProviderId().equals(patientId) || n.getProviderType() != ProviderType.PATIENT) {
            throw new UnauthorizedException("You are not authorized to access this notification");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    private PatientNotificationResponse toResponse(Notification n) {
        return PatientNotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
