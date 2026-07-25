package com.partner.backend.admin.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminProviderDeletionService {

    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LabRepository labRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        doctor.setDeleted(true);
        doctor.setDeletedAt(LocalDateTime.now());
        doctorRepository.save(doctor);
        deactivateUser(doctor.getUser());
    }

    @Transactional
    public void deletePharmacy(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", id));
        pharmacy.setDeleted(true);
        pharmacy.setDeletedAt(LocalDateTime.now());
        pharmacyRepository.save(pharmacy);
        deactivateUser(pharmacy.getUser());
    }

    @Transactional
    public void deleteLab(Long id) {
        Lab lab = labRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", id));
        lab.setDeleted(true);
        lab.setDeletedAt(LocalDateTime.now());
        labRepository.save(lab);
        deactivateUser(lab.getUser());
    }

    private void deactivateUser(User user) {
        if (user == null) {
            return;
        }
        user.setActive(false);
        userRepository.save(user);
        String email = user.getEmail();
        if (email != null && !email.isBlank()) {
            redisTemplate.delete("LOGIN_USER:" + email.trim().toLowerCase());
        }
    }

    @Transactional
    public void restoreDoctor(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted doctor", id));
        doctor.setDeleted(false);
        doctor.setDeletedAt(null);
        doctorRepository.save(doctor);
        activateUser(doctor.getUser());
    }

    @Transactional
    public void restorePharmacy(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted pharmacy", id));
        pharmacy.setDeleted(false);
        pharmacy.setDeletedAt(null);
        pharmacyRepository.save(pharmacy);
        activateUser(pharmacy.getUser());
    }

    @Transactional
    public void restoreLab(Long id) {
        Lab lab = labRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deleted lab", id));
        lab.setDeleted(false);
        lab.setDeletedAt(null);
        labRepository.save(lab);
        activateUser(lab.getUser());
    }

    private void activateUser(User user) {
        if (user == null) {
            return;
        }
        user.setActive(true);
        userRepository.save(user);
    }
}
