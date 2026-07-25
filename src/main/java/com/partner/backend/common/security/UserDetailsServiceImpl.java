package com.partner.backend.common.security;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LabRepository labRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Long providerId = resolveProviderId(user);
        return new CustomUserDetails(user, providerId);
    }

    private Long resolveProviderId(User user) {
        return switch (user.getRole()) {
            case DOCTOR -> doctorRepository.findByUserId(user.getId())
                    .map(d -> d.getId()).orElse(null);
            case PHARMACY -> pharmacyRepository.findByUserId(user.getId())
                    .map(p -> p.getId()).orElse(null);
            case LAB -> labRepository.findByUserId(user.getId())
                    .map(l -> l.getId()).orElse(null);
            case PATIENT -> patientRepository.findByUser_Id(user.getId())
                    .map(Patient::getId).orElse(null);
            default -> null;
        };
    }
}
