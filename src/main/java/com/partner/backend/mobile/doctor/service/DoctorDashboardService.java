package com.partner.backend.mobile.doctor.service;

import com.partner.backend.common.entity.AppointmentStatus;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.doctor.dto.AppointmentResponse;
import com.partner.backend.mobile.doctor.dto.DoctorDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorDashboardService {

    private final AppointmentRepository appointmentRepository;

    public DoctorDashboardResponse getDashboard(Long doctorId) {
        long total = appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.SCHEDULED)
                + appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.IN_PROGRESS)
                + appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED);
        long pending = appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.SCHEDULED);
        long completed = appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED);
        long active = appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.IN_PROGRESS);

        Double rawEarnings = appointmentRepository.sumFeeByDoctorId(doctorId);
        BigDecimal earnings = rawEarnings != null ? BigDecimal.valueOf(rawEarnings) : BigDecimal.ZERO;

        List<AppointmentResponse> today = appointmentRepository
                .findByDoctorIdAndDate(doctorId, LocalDate.now())
                .stream().map(a -> {
                    Integer age = null;
                    String gender = null;
                    if (a.getPatient() != null) {
                        age = a.getPatient().getAge();
                        gender = a.getPatient().getGender();
                    }
                    return AppointmentResponse.builder()
                            .id(a.getId())
                            .patientName(a.getPatientName())
                            .patientPhone(a.getPatientPhone())
                            .patientAge(age)
                            .patientGender(gender)
                            .date(a.getDate())
                            .timeSlot(a.getTimeSlot())
                            .type(a.getType())
                            .status(a.getStatus())
                            .fee(a.getFee())
                            .createdAt(a.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return DoctorDashboardResponse.builder()
                .totalAppointments(total)
                .pendingAppointments(pending)
                .completedAppointments(completed)
                .activeConsultations(active)
                .totalEarnings(earnings)
                .todaysAppointments(today)
                .build();
    }
}
