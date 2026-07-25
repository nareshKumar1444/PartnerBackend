package com.partner.backend.common.service;

import com.partner.backend.common.exception.BadRequestException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:}")
    private String configuredFromAddress;

    @Value("${app.mail.from-name:Health Wallet Partner}")
    private String fromName;

    @Value("${app.mail.console-otp-instead-of-smtp:false}")
    private boolean consoleOtpInsteadOfSmtp;

    private String effectiveFromAddress() {
        if (StringUtils.hasText(configuredFromAddress)) {
            return configuredFromAddress.trim();
        }
        if (StringUtils.hasText(mailUsername)) {
            return mailUsername.trim();
        }
        return "";
    }

    /**
     * Sends OTP email on a worker thread so HTTP can return immediately after the OTP row is committed.
     * Mobile clients often use ~45s fetch timeouts; blocking on SMTP (same order of latency) caused false
     * "request timed out" even when the phone could reach the API.
     */
    @Async
    public void sendOtpAsync(String toEmail, String recipientName, String otpCode, int expiryMinutes) {
        try {
            sendOtp(toEmail, recipientName, otpCode, expiryMinutes);
        } catch (Exception e) {
            log.error("[EMAIL-OTP][async] Failed sending OTP to {} — check SMTP or set MAIL_CONSOLE_OTP=true for local dev", toEmail, e);
        }
    }

    /** Synchronous send (used by {@link #sendOtpAsync} and anywhere mail must complete before returning). */
    public void sendOtp(String toEmail, String recipientName, String otpCode, int expiryMinutes) {
        if (consoleOtpInsteadOfSmtp) {
            log.warn(
                    "[EMAIL-OTP][dev] MAIL_CONSOLE_OTP=true — OTP for {} is {} ({} min TTL). Do not use in production.",
                    toEmail,
                    otpCode,
                    expiryMinutes);
            return;
        }
        String fromAddr = effectiveFromAddress();
        if (!StringUtils.hasText(mailUsername)) {
            throw new BadRequestException(
                    "Email is not configured: set MAIL_USERNAME and MAIL_PASSWORD, or use MAIL_CONSOLE_OTP=true locally.");
        }
        if (!StringUtils.hasText(fromAddr)) {
            throw new BadRequestException("Set MAIL_FROM or MAIL_USERNAME so the sender address is known.");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddr, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Your Health Wallet OTP Code");
            helper.setText(buildOtpHtml(recipientName, otpCode, expiryMinutes), true);
            mailSender.send(message);
            log.info("[EMAIL-OTP] Sent OTP to {}", toEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[EMAIL-OTP] Failed building OTP mime for {}", toEmail, e);
            throw new BadRequestException(
                    "Unable to prepare OTP email. Check MAIL_FROM address format and SMTP settings.");
        } catch (MailException e) {
            log.error("[EMAIL-OTP] Spring Mail failed sending OTP to {}", toEmail, e);
            throw new BadRequestException(smtpFailureUserMessage(e));
        }
    }

    /** Surface Gmail/auth/network causes so the mobile app shows a usable message instead of a generic 500. */
    private static String smtpFailureUserMessage(MailException e) {
        Throwable root = e;
        String chain = "";
        while (root != null) {
            chain += root.getMessage() == null ? "" : root.getMessage().toLowerCase();
            Throwable next = root.getCause();
            if (next == null || next == root) {
                break;
            }
            root = next;
        }
        if (chain.contains("authentication")
                || chain.contains("authenticate")
                || chain.contains("535")
                || chain.contains("credentials")) {
            return "SMTP login failed. For Gmail: turn on 2-Step Verification, create an App Password, and set MAIL_USERNAME / MAIL_PASSWORD on the server.";
        }
        if (chain.contains("connection timed out")
                || chain.contains("timeout")
                || chain.contains("timed out")) {
            return "Mail server unreachable (timeout). Check network, MAIL_HOST / MAIL_PORT, and firewall.";
        }
        Throwable last = lastCause(e);
        String msg = last.getMessage();
        String hint = msg != null && !msg.isBlank() ? msg : "unknown SMTP error";
        return "Unable to send OTP email: " + hint;
    }

    private static Throwable lastCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }

    @Async
    public void sendProviderApproval(String toEmail, String providerName, String role) {
        String fromAddr = effectiveFromAddress();
        if (!StringUtils.hasText(fromAddr)) {
            log.warn("[EMAIL] Skip approval mail (no MAIL_FROM / MAIL_USERNAME): {}", toEmail);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddr, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Your Health Wallet Account Has Been Approved!");
            helper.setText(buildApprovalHtml(providerName, role), true);
            mailSender.send(message);
            log.info("[EMAIL] Sent approval email to {}", toEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException | MailException e) {
            log.error("[EMAIL] Failed to send approval email to {}", toEmail, e);
        }
    }

    @Async
    public void sendProviderRejection(String toEmail, String providerName, String reason) {
        String fromAddr = effectiveFromAddress();
        if (!StringUtils.hasText(fromAddr)) {
            log.warn("[EMAIL] Skip rejection mail (no MAIL_FROM / MAIL_USERNAME): {}", toEmail);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddr, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Health Wallet Account Verification Update");
            helper.setText(buildRejectionHtml(providerName, reason), true);
            mailSender.send(message);
            log.info("[EMAIL] Sent rejection email to {}", toEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException | MailException e) {
            log.error("[EMAIL] Failed to send rejection email to {}", toEmail, e);
        }
    }

    @Async
    public void sendAppointmentReminderToPatient(
            String toEmail,
            String patientName,
            String doctorName,
            String dateLabel,
            String timeSlot,
            String doctorPhone,
            String venueLabel) {
        sendAppointmentEmail(
                toEmail,
                "Appointment reminder",
                buildAppointmentReminderHtml(
                        patientName,
                        "Your appointment is coming up in 5 minutes.",
                        doctorName,
                        dateLabel,
                        timeSlot,
                        doctorPhone,
                        venueLabel));
    }

    @Async
    public void sendAppointmentReminderToDoctor(
            String toEmail,
            String doctorName,
            String patientName,
            String dateLabel,
            String timeSlot,
            String patientPhone,
            String venueLabel) {
        sendAppointmentEmail(
                toEmail,
                "Appointment reminder",
                buildAppointmentReminderHtml(
                        doctorName,
                        "You have an appointment in 5 minutes.",
                        patientName,
                        dateLabel,
                        timeSlot,
                        patientPhone,
                        venueLabel));
    }

    @Async
    public void sendAppointmentAutoCancelledToPatient(
            String toEmail,
            String patientName,
            String doctorName,
            String dateLabel,
            String timeSlot) {
        sendAppointmentEmail(
                toEmail,
                "Appointment cancelled",
                """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:0;">
                  <div style="max-width:520px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">
                    <div style="background:#e53935;padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;font-size:22px;">Appointment cancelled</h2>
                    </div>
                    <div style="padding:32px;">
                      <p style="color:#333;font-size:16px;">Hi <strong>%s</strong>,</p>
                      <p style="color:#555;font-size:15px;">Your appointment with <strong>%s</strong> on <strong>%s</strong> at <strong>%s</strong> was automatically cancelled because the time passed.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                        patientName != null && !patientName.isBlank() ? patientName : "patient",
                        doctorName != null && !doctorName.isBlank() ? doctorName : "your doctor",
                        dateLabel,
                        timeSlot));
    }

    @Async
    public void sendPharmacyNewOrder(
            String toEmail,
            String pharmacyName,
            Long orderId,
            String patientName,
            String patientPhone,
            String deliveryAddress,
            String paymentMethodLabel,
            String orderItemsHtml,
            String deliveryFeeLabel,
            String totalAmountLabel) {
        sendOrderStatusEmail(
                toEmail,
                "New medicine order #" + orderId,
                buildPharmacyNewOrderHtml(
                        pharmacyName,
                        orderId,
                        patientName,
                        patientPhone,
                        deliveryAddress,
                        paymentMethodLabel,
                        orderItemsHtml,
                        deliveryFeeLabel,
                        totalAmountLabel));
    }

    @Async
    public void sendPatientOrderAccepted(
            String toEmail,
            String patientName,
            String pharmacyName,
            Long orderId,
            String deliveryAddress) {
        sendOrderStatusEmail(
                toEmail,
                "Your medicine order was accepted",
                buildOrderStatusHtml(
                        patientName,
                        pharmacyName + " has accepted your order #" + orderId + ".",
                        orderId,
                        pharmacyName,
                        deliveryAddress,
                        null,
                        null,
                        "#00B4B4",
                        "#007A7A",
                        "Order accepted"));
    }

    @Async
    public void sendPatientOrderRejected(
            String toEmail,
            String patientName,
            String pharmacyName,
            Long orderId,
            String deliveryAddress) {
        sendOrderStatusEmail(
                toEmail,
                "Your medicine order was rejected",
                buildOrderStatusHtml(
                        patientName,
                        pharmacyName + " could not fulfill your order #" + orderId + ".",
                        orderId,
                        pharmacyName,
                        deliveryAddress,
                        null,
                        null,
                        "#e53935",
                        "#c62828",
                        "Order rejected"));
    }

    @Async
    public void sendPatientOrderCompleted(
            String toEmail,
            String patientName,
            String pharmacyName,
            Long orderId,
            String deliveryAddress,
            String riderName,
            String riderPhone) {
        sendOrderStatusEmail(
                toEmail,
                "Your medicine order is on the way",
                buildOrderStatusHtml(
                        patientName,
                        pharmacyName + " has completed your order #" + orderId
                                + " and assigned a rider for delivery.",
                        orderId,
                        pharmacyName,
                        deliveryAddress,
                        riderName,
                        riderPhone,
                        "#00B4B4",
                        "#007A7A",
                        "Order completed"));
    }

    // ===== LAB APPOINTMENT EMAILS =====

    @Async
    public void sendLabNewBookingToLab(
            String toEmail,
            String labOwnerName,
            String patientName,
            String testName,
            String dateLabel,
            String timeSlot,
            String patientPhone) {
        sendLabEmail(toEmail, "New lab test booking",
                buildLabNewBookingHtml(labOwnerName, patientName, testName, dateLabel, timeSlot, patientPhone));
    }

    @Async
    public void sendLabBookingConfirmedToPatient(
            String toEmail,
            String patientName,
            String labName,
            String testName,
            String dateLabel,
            String timeSlot) {
        sendLabEmail(toEmail, "Your lab test has been confirmed",
                buildLabStatusEmailHtml(patientName, labName, testName, dateLabel, timeSlot,
                        "Test Confirmed", "#1565C0", "#0D47A1",
                        labName + " has confirmed your " + testName + " test booking."));
    }

    @Async
    public void sendLabBookingCancelledToPatient(
            String toEmail,
            String patientName,
            String labName,
            String testName,
            String dateLabel,
            String timeSlot) {
        sendLabEmail(toEmail, "Your lab test booking has been cancelled",
                buildLabStatusEmailHtml(patientName, labName, testName, dateLabel, timeSlot,
                        "Booking Cancelled", "#e53935", "#c62828",
                        labName + " has cancelled your " + testName + " test booking."));
    }

    @Async
    public void sendLabTestCompletedToPatient(
            String toEmail,
            String patientName,
            String labName,
            String testName,
            String dateLabel) {
        sendLabEmail(toEmail, "Your lab test is completed",
                buildLabStatusEmailHtml(patientName, labName, testName, dateLabel, null,
                        "Test Completed", "#2e7d32", "#1b5e20",
                        "Your " + testName + " test at " + labName + " has been completed. Your report will be available soon."));
    }

    @Async
    public void sendLabBookingAutoCancelledToPatient(
            String toEmail,
            String patientName,
            String labName,
            String testName,
            String dateLabel,
            String timeSlot) {
        sendLabEmail(toEmail, "Lab test booking auto-cancelled (no-show)",
                buildLabStatusEmailHtml(patientName, labName, testName, dateLabel, timeSlot,
                        "Booking Auto-Cancelled", "#e53935", "#c62828",
                        "Your " + testName + " test was automatically cancelled because you did not visit "
                                + labName + " on the scheduled date."));
    }

    private void sendLabEmail(String toEmail, String subject, String html) {
        if (!StringUtils.hasText(toEmail)) {
            return;
        }
        String fromAddr = effectiveFromAddress();
        if (!StringUtils.hasText(fromAddr)) {
            log.warn("[EMAIL] Skip lab mail (no MAIL_FROM / MAIL_USERNAME): {}", toEmail);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddr, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[EMAIL] Sent lab mail ({}) to {}", subject, toEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException | MailException e) {
            log.error("[EMAIL] Failed to send lab mail to {}", toEmail, e);
        }
    }

    private String buildLabNewBookingHtml(
            String labOwnerName,
            String patientName,
            String testName,
            String dateLabel,
            String timeSlot,
            String patientPhone) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:0;">
                  <div style="max-width:520px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#1565C0,#0D47A1);padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;font-size:22px;">New Lab Test Booking</h2>
                    </div>
                    <div style="padding:32px;">
                      <p style="color:#333;font-size:16px;">Hi <strong>%s</strong>,</p>
                      <p style="color:#555;font-size:15px;">A patient has booked a lab test at your facility.</p>
                      <div style="background:#f7fafc;border-radius:10px;padding:18px 20px;margin:24px 0;">
                        <p style="margin:0 0 8px;color:#333;"><strong>Booking Details</strong></p>
                        <p style="margin:0;color:#555;">Patient: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Phone: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Test: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Date: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Time: %s</p>
                      </div>
                      <p style="color:#888;font-size:13px;">Please open the Health Wallet Partner app to confirm or cancel this booking.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                labOwnerName != null && !labOwnerName.isBlank() ? labOwnerName : "Lab",
                patientName != null && !patientName.isBlank() ? patientName : "Patient",
                patientPhone != null && !patientPhone.isBlank() ? patientPhone : "—",
                testName != null && !testName.isBlank() ? testName : "Lab Test",
                dateLabel != null ? dateLabel : "—",
                timeSlot != null && !timeSlot.isBlank() ? timeSlot : "—");
    }

    private String buildLabStatusEmailHtml(
            String patientName,
            String labName,
            String testName,
            String dateLabel,
            String timeSlot,
            String headerTitle,
            String colorStart,
            String colorEnd,
            String leadText) {
        String timeRow = (timeSlot != null && !timeSlot.isBlank())
                ? "<p style=\"margin:8px 0 0;color:#555;\">Time: " + timeSlot + "</p>"
                : "";
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:0;">
                  <div style="max-width:520px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,%s,%s);padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;font-size:22px;">%s</h2>
                    </div>
                    <div style="padding:32px;">
                      <p style="color:#333;font-size:16px;">Hi <strong>%s</strong>,</p>
                      <p style="color:#555;font-size:15px;">%s</p>
                      <div style="background:#f7fafc;border-radius:10px;padding:18px 20px;margin:24px 0;">
                        <p style="margin:0 0 8px;color:#333;"><strong>Test Details</strong></p>
                        <p style="margin:0;color:#555;">Laboratory: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Test: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Date: %s</p>
                        %s
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                colorStart,
                colorEnd,
                headerTitle,
                patientName != null && !patientName.isBlank() ? patientName : "there",
                leadText,
                labName != null && !labName.isBlank() ? labName : "Laboratory",
                testName != null && !testName.isBlank() ? testName : "Lab Test",
                dateLabel != null ? dateLabel : "—",
                timeRow);
    }

    private void sendOrderStatusEmail(String toEmail, String subject, String html) {
        if (!StringUtils.hasText(toEmail)) {
            return;
        }
        String fromAddr = effectiveFromAddress();
        if (!StringUtils.hasText(fromAddr)) {
            log.warn("[EMAIL] Skip order status mail (no MAIL_FROM / MAIL_USERNAME): {}", toEmail);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddr, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[EMAIL] Sent order status mail ({}) to {}", subject, toEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException | MailException e) {
            log.error("[EMAIL] Failed to send order status mail to {}", toEmail, e);
        }
    }

    private String buildOrderStatusHtml(
            String patientName,
            String leadText,
            Long orderId,
            String pharmacyName,
            String deliveryAddress,
            String riderName,
            String riderPhone,
            String headerColorStart,
            String headerColorEnd,
            String headerTitle) {
        String riderBlock = "";
        if (StringUtils.hasText(riderName)) {
            riderBlock = """
                    <p style="margin:8px 0 0;color:#555;">Rider: %s</p>
                    <p style="margin:8px 0 0;color:#555;">Rider phone: %s</p>
                    """.formatted(
                    riderName,
                    StringUtils.hasText(riderPhone) ? riderPhone : "—");
        }
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:0;">
                  <div style="max-width:520px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,%s,%s);padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;font-size:22px;">%s</h2>
                    </div>
                    <div style="padding:32px;">
                      <p style="color:#333;font-size:16px;">Hi <strong>%s</strong>,</p>
                      <p style="color:#555;font-size:15px;">%s</p>
                      <div style="background:#f7fafc;border-radius:10px;padding:18px 20px;margin:24px 0;">
                        <p style="margin:0 0 8px;color:#333;"><strong>Order #%d</strong></p>
                        <p style="margin:0;color:#555;">Pharmacy: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Delivery address: %s</p>
                        %s
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                headerColorStart,
                headerColorEnd,
                headerTitle,
                patientName != null && !patientName.isBlank() ? patientName : "there",
                leadText,
                orderId != null ? orderId : 0L,
                pharmacyName != null && !pharmacyName.isBlank() ? pharmacyName : "Pharmacy",
                deliveryAddress != null && !deliveryAddress.isBlank() ? deliveryAddress : "—",
                riderBlock);
    }

    private String buildPharmacyNewOrderHtml(
            String pharmacyName,
            Long orderId,
            String patientName,
            String patientPhone,
            String deliveryAddress,
            String paymentMethodLabel,
            String orderItemsHtml,
            String deliveryFeeLabel,
            String totalAmountLabel) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:0;">
                  <div style="max-width:560px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#00B4B4,#007A7A);padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;font-size:22px;">New medicine order</h2>
                    </div>
                    <div style="padding:32px;">
                      <p style="color:#333;font-size:16px;">Hi <strong>%s</strong>,</p>
                      <p style="color:#555;font-size:15px;">You received a new order <strong>#%d</strong> from a patient.</p>
                      <div style="background:#f7fafc;border-radius:10px;padding:18px 20px;margin:24px 0;">
                        <p style="margin:0 0 8px;color:#333;"><strong>Patient</strong></p>
                        <p style="margin:0;color:#555;">Name: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Phone: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Delivery address: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Payment method: %s</p>
                      </div>
                      <p style="color:#333;font-size:15px;margin:0 0 12px;"><strong>Ordered medicines</strong></p>
                      <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                        <thead>
                          <tr style="background:#eef7f7;">
                            <th style="padding:10px 8px;text-align:left;color:#333;">Medicine</th>
                            <th style="padding:10px 8px;text-align:center;color:#333;">Qty</th>
                            <th style="padding:10px 8px;text-align:right;color:#333;">Amount</th>
                          </tr>
                        </thead>
                        <tbody>
                          %s
                        </tbody>
                      </table>
                      <div style="margin-top:20px;padding-top:16px;border-top:1px solid #eee;">
                        <p style="margin:0;color:#555;text-align:right;">Delivery fee: <strong>Rs. %s</strong></p>
                        <p style="margin:10px 0 0;color:#333;text-align:right;font-size:18px;">Total bill: <strong>Rs. %s</strong></p>
                      </div>
                      <p style="color:#888;font-size:13px;margin-top:24px;">Please open the Health Wallet Partner app to accept or reject this order.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                pharmacyName != null && !pharmacyName.isBlank() ? pharmacyName : "Pharmacy",
                orderId != null ? orderId : 0L,
                patientName != null && !patientName.isBlank() ? patientName : "Patient",
                patientPhone != null && !patientPhone.isBlank() ? patientPhone : "—",
                deliveryAddress != null && !deliveryAddress.isBlank() ? deliveryAddress : "—",
                paymentMethodLabel != null && !paymentMethodLabel.isBlank() ? paymentMethodLabel : "—",
                orderItemsHtml != null && !orderItemsHtml.isBlank()
                        ? orderItemsHtml
                        : "<tr><td colspan=\"3\" style=\"padding:8px;color:#555;\">—</td></tr>",
                deliveryFeeLabel != null ? deliveryFeeLabel : "0.00",
                totalAmountLabel != null ? totalAmountLabel : "0.00");
    }

    private void sendAppointmentEmail(String toEmail, String subject, String html) {
        if (!StringUtils.hasText(toEmail)) {
            return;
        }
        String fromAddr = effectiveFromAddress();
        if (!StringUtils.hasText(fromAddr)) {
            log.warn("[EMAIL] Skip appointment mail (no MAIL_FROM / MAIL_USERNAME): {}", toEmail);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddr, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException | MailException e) {
            log.error("[EMAIL] Failed to send appointment mail to {}", toEmail, e);
        }
    }

    private String buildAppointmentReminderHtml(
            String recipientName,
            String leadText,
            String otherPartyName,
            String dateLabel,
            String timeSlot,
            String phoneNumber,
            String venueLabel) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:0;">
                  <div style="max-width:520px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#00B4B4,#007A7A);padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;font-size:22px;">Health Wallet Partner</h2>
                    </div>
                    <div style="padding:32px;">
                      <p style="color:#333;font-size:16px;">Hi <strong>%s</strong>,</p>
                      <p style="color:#555;font-size:15px;">%s</p>
                      <div style="background:#f7fafc;border-radius:10px;padding:18px 20px;margin:24px 0;">
                        <p style="margin:0 0 8px;color:#333;"><strong>%s</strong></p>
                        <p style="margin:0;color:#555;">Date: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Time: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Phone: %s</p>
                        <p style="margin:8px 0 0;color:#555;">Venue: %s</p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                recipientName != null && !recipientName.isBlank() ? recipientName : "there",
                leadText,
                otherPartyName != null && !otherPartyName.isBlank() ? otherPartyName : "Appointment",
                dateLabel,
                timeSlot,
                phoneNumber != null && !phoneNumber.isBlank() ? phoneNumber : "—",
                venueLabel);
    }

    private String buildOtpHtml(String name, String otp, int minutes) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;margin:0;padding:0;">
                  <div style="max-width:480px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#00B4B4,#007A7A);padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;font-size:22px;">Health Wallet Partner</h2>
                    </div>
                    <div style="padding:32px;">
                      <p style="color:#333;font-size:16px;">Hi <strong>%s</strong>,</p>
                      <p style="color:#555;font-size:15px;">Use the code below to verify your email address. This code expires in <strong>%d minutes</strong>.</p>
                      <div style="background:#f0fafa;border:2px dashed #00B4B4;border-radius:10px;text-align:center;padding:24px;margin:24px 0;">
                        <span style="font-size:36px;font-weight:700;letter-spacing:10px;color:#007A7A;">%s</span>
                      </div>
                      <p style="color:#888;font-size:13px;">If you did not request this, please ignore this email.</p>
                    </div>
                    <div style="background:#f9f9f9;padding:16px 32px;text-align:center;">
                      <p style="color:#aaa;font-size:12px;margin:0;">&copy; 2025 Health Wallet. All rights reserved.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(name, minutes, otp);
    }

    private String buildApprovalHtml(String name, String role) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;">
                  <div style="max-width:480px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;">
                    <div style="background:linear-gradient(135deg,#00B4B4,#007A7A);padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;">Account Approved!</h2>
                    </div>
                    <div style="padding:32px;">
                      <p>Hi <strong>%s</strong>,</p>
                      <p>Great news! Your <strong>%s</strong> account on Health Wallet has been <strong style="color:#00B4B4;">approved</strong>.</p>
                      <p>You can now log in to the Health Wallet Partner App and start serving patients.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(name, role);
    }

    private String buildRejectionHtml(String name, String reason) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f5f5f5;">
                  <div style="max-width:480px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;">
                    <div style="background:#e53935;padding:28px 32px;">
                      <h2 style="color:#fff;margin:0;">Verification Update</h2>
                    </div>
                    <div style="padding:32px;">
                      <p>Hi <strong>%s</strong>,</p>
                      <p>Unfortunately, your Health Wallet Partner account could not be approved at this time.</p>
                      <p><strong>Reason:</strong> %s</p>
                      <p>Please update your information and re-apply, or contact support for assistance.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(name, reason == null ? "Insufficient documentation" : reason);
    }
}
