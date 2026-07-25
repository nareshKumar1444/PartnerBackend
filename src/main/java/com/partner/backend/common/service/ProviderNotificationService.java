package com.partner.backend.common.service;

import com.partner.backend.common.entity.Appointment;
import com.partner.backend.common.entity.AppointmentType;
import com.partner.backend.common.entity.LabAppointment;
import com.partner.backend.common.entity.LabAppointmentStatus;
import com.partner.backend.common.entity.Notification;
import com.partner.backend.common.entity.OnlinePaymentChannel;
import com.partner.backend.common.entity.Order;
import com.partner.backend.common.entity.OrderItem;
import com.partner.backend.common.entity.OrderStatus;
import com.partner.backend.common.entity.PaymentMethod;
import com.partner.backend.common.entity.Pharmacy;
import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ProviderNotificationService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final OneSignalPushService oneSignalPushService;

    @Transactional
    public void notifyDoctorNewAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null) {
            return;
        }
        String patientName = appointment.getPatientName();
        if (patientName == null || patientName.isBlank()) {
            patientName = "A patient";
        }
        String typeLabel = appointment.getType() == AppointmentType.VIRTUAL ? "Virtual" : "In-person";
        String dateStr = appointment.getDate() != null ? appointment.getDate().format(DATE_FMT) : "—";
        String slot = appointment.getTimeSlot() != null ? appointment.getTimeSlot() : "—";

        String message = String.format(
                "%s booked a %s appointment on %s at %s.",
                patientName,
                typeLabel,
                dateStr,
                slot);

        saveProviderNotification(
                appointment.getDoctor().getId(),
                ProviderType.DOCTOR,
                "New appointment booked",
                message);
    }

    @Transactional
    public void notifyPatientAppointmentCancelled(Appointment appointment) {
        Long patientId = patientIdFrom(appointment);
        if (patientId == null) {
            return;
        }
        String doctorName = appointment.getDoctor() != null ? appointment.getDoctor().getName() : "Doctor";
        String message = formatAppointmentMessage(doctorName, "cancelled", appointment);
        savePatientNotification(patientId, "Appointment cancelled", message);
    }

    @Transactional
    public void notifyPatientAppointmentCompleted(Appointment appointment) {
        Long patientId = patientIdFrom(appointment);
        if (patientId == null) {
            return;
        }
        String doctorName = appointment.getDoctor() != null ? appointment.getDoctor().getName() : "Doctor";
        String message = formatAppointmentMessage(doctorName, "completed", appointment);
        savePatientNotification(patientId, "Consultation completed", message);
    }

    @Transactional
    public void notifyPatientAppointmentAutoCancelled(Appointment appointment) {
        Long patientId = patientIdFrom(appointment);
        if (patientId == null) {
            return;
        }
        String doctorName = appointment.getDoctor() != null && appointment.getDoctor().getName() != null
                ? appointment.getDoctor().getName() : "your doctor";
        String dateStr = appointment.getDate() != null ? appointment.getDate().format(DATE_FMT) : "—";
        String slot = appointment.getTimeSlot() != null ? appointment.getTimeSlot() : "—";
        savePatientNotification(
                patientId,
                "Appointment cancelled",
                String.format(
                        "Your appointment with Dr. %s on %s at %s was automatically cancelled because the time slot has passed.",
                        doctorName, dateStr, slot));
    }

    @Transactional
    public void notifyPatientLabAutoCancelled(LabAppointment labAppointment) {
        if (labAppointment == null || labAppointment.getPatient() == null) {
            return;
        }
        String labName = labAppointment.getLab() != null && labAppointment.getLab().getName() != null
                ? labAppointment.getLab().getName() : "the lab";
        String testName = labAppointment.getTest() != null && labAppointment.getTest().getTestName() != null
                ? labAppointment.getTest().getTestName() : "your lab test";
        String dateStr = labAppointment.getScheduledDate() != null
                ? labAppointment.getScheduledDate().format(DATE_FMT) : "—";
        savePatientNotification(
                labAppointment.getPatient().getId(),
                "Lab booking cancelled",
                String.format(
                        "Your booking for %s at %s on %s was automatically cancelled because the date has passed.",
                        testName, labName, dateStr));
    }

    @Transactional
    public void notifyLabNewAppointment(LabAppointment labAppointment) {
        if (labAppointment == null || labAppointment.getLab() == null) {
            return;
        }
        String patientName = labAppointment.getPatientName();
        if (patientName == null || patientName.isBlank()) {
            patientName = "A patient";
        }
        String testName = labAppointment.getTest() != null && labAppointment.getTest().getTestName() != null
                ? labAppointment.getTest().getTestName() : "a lab test";
        String dateStr = labAppointment.getScheduledDate() != null
                ? labAppointment.getScheduledDate().format(DATE_FMT) : "—";
        String slot = labAppointment.getScheduledTimeSlot() != null
                ? labAppointment.getScheduledTimeSlot() : "—";

        String message = String.format(
                "%s booked %s on %s at %s.",
                patientName, testName, dateStr, slot);

        saveProviderNotification(
                labAppointment.getLab().getId(),
                ProviderType.LAB,
                "New lab test booking",
                message);
    }

    @Transactional
    public void notifyPharmacyNewOrder(Order order) {
        if (order == null || order.getPharmacy() == null) {
            return;
        }
        String patientName = order.getPatientName();
        if (patientName == null || patientName.isBlank()) {
            patientName = "A patient";
        }
        String message = String.format(
                "%s placed order #%d%s.",
                patientName,
                order.getId(),
                order.getDeliveryAddress() != null && !order.getDeliveryAddress().isBlank()
                        ? " for " + order.getDeliveryAddress()
                        : "");
        saveProviderNotification(
                order.getPharmacy().getId(),
                ProviderType.PHARMACY,
                "New medicine order",
                message);
        emailService.sendPharmacyNewOrder(
                resolvePharmacyEmail(order),
                resolvePharmacyRecipientName(order),
                order.getId(),
                patientName,
                order.getPatientPhone(),
                order.getDeliveryAddress(),
                formatPaymentMethod(order),
                buildOrderItemsHtml(order),
                formatMoney(order.getDeliveryFee()),
                formatMoney(order.getTotalAmount()));
    }

    @Transactional
    public void notifyPatientOrderStatus(Order order, OrderStatus previousStatus) {
        if (order == null || order.getPatient() == null || order.getStatus() == null) {
            return;
        }
        if (previousStatus == order.getStatus()) {
            return;
        }
        Long patientId = order.getPatient().getId();
        String pharmacyName = order.getPharmacy() != null && order.getPharmacy().getName() != null
                ? order.getPharmacy().getName()
                : "Pharmacy";
        String patientName = resolvePatientName(order);
        String patientEmail = resolvePatientEmail(order);
        String deliveryAddress = order.getDeliveryAddress();

        switch (order.getStatus()) {
            case ACCEPTED -> {
                savePatientNotification(
                        patientId,
                        "Order accepted",
                        String.format("%s accepted your order #%d.", pharmacyName, order.getId()));
                emailService.sendPatientOrderAccepted(
                        patientEmail, patientName, pharmacyName, order.getId(), deliveryAddress);
            }
            case REJECTED -> {
                savePatientNotification(
                        patientId,
                        "Order rejected",
                        String.format("%s rejected your order #%d.", pharmacyName, order.getId()));
                emailService.sendPatientOrderRejected(
                        patientEmail, patientName, pharmacyName, order.getId(), deliveryAddress);
            }
            case COMPLETED -> {
                String riderInfo = (order.getRiderName() != null && !order.getRiderName().isBlank())
                        ? String.format(" Rider: %s%s.",
                        order.getRiderName(),
                        order.getRiderPhone() != null && !order.getRiderPhone().isBlank()
                                ? " (" + order.getRiderPhone() + ")"
                                : "")
                        : "";
                savePatientNotification(
                        patientId,
                        "Order out for delivery",
                        String.format("%s completed order #%d.%s",
                                pharmacyName,
                                order.getId(),
                                riderInfo));
                emailService.sendPatientOrderCompleted(
                        patientEmail,
                        patientName,
                        pharmacyName,
                        order.getId(),
                        deliveryAddress,
                        order.getRiderName(),
                        order.getRiderPhone());
            }
            case CANCELLED -> savePatientNotification(
                    patientId,
                    "Order cancelled",
                    String.format("%s cancelled your order #%d.", pharmacyName, order.getId()));
            default -> {
                // Other status updates are informational for pharmacy flow only.
            }
        }
    }

    @Transactional
    public void notifyPatientLabStatus(LabAppointment labAppointment, LabAppointmentStatus newStatus) {
        if (labAppointment == null || labAppointment.getPatient() == null || newStatus == null) {
            return;
        }
        Long patientId = labAppointment.getPatient().getId();
        String labName = labAppointment.getLab() != null && labAppointment.getLab().getName() != null
                ? labAppointment.getLab().getName() : "Laboratory";
        String testName = labAppointment.getTest() != null && labAppointment.getTest().getTestName() != null
                ? labAppointment.getTest().getTestName() : "your lab test";
        String dateStr = labAppointment.getScheduledDate() != null
                ? labAppointment.getScheduledDate().format(DATE_FMT) : "—";
        String slot = labAppointment.getScheduledTimeSlot() != null
                ? labAppointment.getScheduledTimeSlot() : "—";

        switch (newStatus) {
            case CONFIRMED -> savePatientNotification(
                    patientId,
                    "Lab booking confirmed",
                    String.format("%s confirmed %s on %s at %s.", labName, testName, dateStr, slot));
            case CANCELLED -> savePatientNotification(
                    patientId,
                    "Lab booking cancelled",
                    String.format("%s cancelled %s on %s at %s.", labName, testName, dateStr, slot));
            case COMPLETED -> savePatientNotification(
                    patientId,
                    "Lab test completed",
                    String.format("%s completed %s. Your report will be shared soon.", labName, testName));
            default -> {
                // PENDING — no patient notification needed.
            }
        }
    }

    private String resolvePatientName(Order order) {
        if (order.getPatientName() != null && !order.getPatientName().isBlank()) {
            return order.getPatientName();
        }
        if (order.getPatient() != null && order.getPatient().getName() != null) {
            return order.getPatient().getName();
        }
        return "Patient";
    }

    private String resolvePatientEmail(Order order) {
        if (order.getPatient() == null || order.getPatient().getEmail() == null) {
            return null;
        }
        String email = order.getPatient().getEmail().trim();
        return email.isBlank() ? null : email;
    }

    private String resolvePharmacyEmail(Order order) {
        Pharmacy pharmacy = order.getPharmacy();
        if (pharmacy == null) {
            return null;
        }
        if (pharmacy.getEmail() != null && !pharmacy.getEmail().isBlank()) {
            return pharmacy.getEmail().trim();
        }
        if (pharmacy.getUser() != null
                && pharmacy.getUser().getEmail() != null
                && !pharmacy.getUser().getEmail().isBlank()) {
            return pharmacy.getUser().getEmail().trim();
        }
        return null;
    }

    private String resolvePharmacyRecipientName(Order order) {
        Pharmacy pharmacy = order.getPharmacy();
        if (pharmacy == null) {
            return "Pharmacy";
        }
        if (pharmacy.getOwnerName() != null && !pharmacy.getOwnerName().isBlank()) {
            return pharmacy.getOwnerName().trim();
        }
        if (pharmacy.getName() != null && !pharmacy.getName().isBlank()) {
            return pharmacy.getName().trim();
        }
        return "Pharmacy";
    }

    private String formatPaymentMethod(Order order) {
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            return "Cash on Delivery";
        }
        if (order.getPaymentMethod() == PaymentMethod.ONLINE) {
            if (order.getOnlinePaymentChannel() == OnlinePaymentChannel.CARD) {
                return "Online — Card";
            }
            if (order.getOnlinePaymentChannel() == OnlinePaymentChannel.BANK) {
                String bank = order.getPaymentBankName();
                if (bank != null && !bank.isBlank()) {
                    return "Online — Bank (" + bank.trim() + ")";
                }
                return "Online — Bank Transfer";
            }
            return "Online";
        }
        return "—";
    }

    private String buildOrderItemsHtml(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return "";
        }
        StringBuilder rows = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            String medicineName = item.getMedicineName() != null && !item.getMedicineName().isBlank()
                    ? escapeHtml(item.getMedicineName())
                    : "Medicine";
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            rows.append("""
                    <tr>
                      <td style="padding:8px;border-bottom:1px solid #eee;color:#555;">%s</td>
                      <td style="padding:8px;border-bottom:1px solid #eee;text-align:center;color:#555;">%d</td>
                      <td style="padding:8px;border-bottom:1px solid #eee;text-align:right;color:#555;">Rs. %s</td>
                    </tr>
                    """.formatted(medicineName, item.getQuantity(), formatMoney(lineTotal)));
        }
        return rows.toString();
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private Long patientIdFrom(Appointment appointment) {
        if (appointment == null || appointment.getPatient() == null) {
            return null;
        }
        return appointment.getPatient().getId();
    }

    private String formatAppointmentMessage(String doctorName, String action, Appointment appointment) {
        String dateStr = appointment.getDate() != null ? appointment.getDate().format(DATE_FMT) : "—";
        String slot = appointment.getTimeSlot() != null ? appointment.getTimeSlot() : "—";
        return String.format(
                "Dr. %s has %s your appointment on %s at %s.",
                doctorName != null && !doctorName.isBlank() ? doctorName : "your doctor",
                action,
                dateStr,
                slot);
    }

    private void savePatientNotification(Long patientId, String title, String message) {
        saveProviderNotification(patientId, ProviderType.PATIENT, title, message);
    }

    private void saveProviderNotification(Long providerId, ProviderType providerType, String title, String message) {
        notificationRepository.save(Notification.builder()
                .providerId(providerId)
                .providerType(providerType)
                .title(title)
                .message(message)
                .read(false)
                .build());
        oneSignalPushService.sendProviderPushAsync(providerId, providerType, title, message);
    }
}
