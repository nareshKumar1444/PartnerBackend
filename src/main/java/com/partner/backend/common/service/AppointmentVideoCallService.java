package com.partner.backend.common.service;

import com.partner.backend.common.dto.VideoCallSessionResponse;
import com.partner.backend.common.entity.Appointment;
import com.partner.backend.common.entity.AppointmentStatus;
import com.partner.backend.common.entity.AppointmentType;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.exception.UnauthorizedException;
import com.partner.backend.common.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentVideoCallService {

    private final AppointmentRepository appointmentRepository;
    private final JitsiMeetService jitsiMeetService;

    @Transactional
    public VideoCallSessionResponse createDoctorSession(Long doctorId, Long appointmentId, boolean audioOnly) {
        Appointment appointment = loadAppointment(appointmentId);
        assertDoctorAccess(appointment, doctorId);
        assertVirtualCallAllowed(appointment);

        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointment.setStatus(AppointmentStatus.IN_PROGRESS);
            appointmentRepository.save(appointment);
        }

        String displayName = appointment.getDoctor().getName();
        if (displayName == null || displayName.isBlank()) {
            displayName = "Doctor";
        }
        return buildSession(appointment, displayName, audioOnly, true);
    }

    @Transactional(readOnly = true)
    public VideoCallSessionResponse createPatientSession(Long patientId, Long appointmentId, boolean audioOnly) {
        Appointment appointment = loadAppointment(appointmentId);
        assertPatientAccess(appointment, patientId);
        assertVirtualCallAllowed(appointment);
        assertPatientMayJoin(appointment);

        String displayName = appointment.getPatientName();
        if (displayName == null || displayName.isBlank()) {
            displayName = "Patient";
        }
        return buildSession(appointment, displayName, audioOnly, false);
    }

    private Appointment loadAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
    }

    private void assertDoctorAccess(Appointment appointment, Long doctorId) {
        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedException("You are not authorized to access this appointment");
        }
    }

    private void assertPatientAccess(Appointment appointment, Long patientId) {
        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId)) {
            throw new UnauthorizedException("You are not authorized to access this appointment");
        }
    }

    private void assertVirtualCallAllowed(Appointment appointment) {
        if (appointment.getType() != AppointmentType.VIRTUAL) {
            throw new BadRequestException("Video calls are only available for virtual appointments");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("This appointment was cancelled");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("This consultation is already completed");
        }
    }

    private void assertPatientMayJoin(Appointment appointment) {
        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS
                && appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("This appointment is not available for a video call right now");
        }
    }

    private VideoCallSessionResponse buildSession(
            Appointment appointment, String displayName, boolean audioOnly, boolean moderator) {
        String roomName = jitsiMeetService.roomNameForAppointment(appointment.getId());
        String jwt = jitsiMeetService.issueToken(roomName, displayName, moderator);
        String joinUrl = jitsiMeetService.buildJoinUrl(roomName, displayName, audioOnly, moderator, jwt);
        return VideoCallSessionResponse.builder()
                .appointmentId(appointment.getId())
                .roomName(roomName)
                .externalRoomName(jitsiMeetService.externalApiRoomName(roomName))
                .serverUrl(jitsiMeetService.normalizedServerUrl())
                .joinUrl(joinUrl)
                .displayName(displayName)
                .audioOnly(audioOnly)
                .jwt(jwt)
                .moderator(moderator)
                .jaasEnabled(jitsiMeetService.usesJaas())
                .build();
    }
}
