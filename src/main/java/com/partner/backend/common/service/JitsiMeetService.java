package com.partner.backend.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class JitsiMeetService {

    private final JitsiJaasTokenService jaasTokenService;

    @org.springframework.beans.factory.annotation.Value("${app.jitsi.server-url:https://meet.jit.si}")
    private String serverUrl;

    @org.springframework.beans.factory.annotation.Value("${app.jitsi.room-prefix:HealthPortalHubAppt}")
    private String roomPrefix;

    public String roomNameForAppointment(Long appointmentId) {
        return roomPrefix + appointmentId;
    }

    public boolean usesJaas() {
        return jaasTokenService.isFullyConfigured();
    }

    public String normalizedServerUrl() {
        if (jaasTokenService.isFullyConfigured()) {
            return "https://8x8.vc";
        }
        String base = serverUrl == null ? "https://meet.jit.si" : serverUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    public String serverHost() {
        String base = normalizedServerUrl();
        if (base.startsWith("https://")) {
            return base.substring("https://".length());
        }
        if (base.startsWith("http://")) {
            return base.substring("http://".length());
        }
        return base;
    }

    /** Room path for Jitsi External API when using JaaS (includes app id prefix). */
    public String externalApiRoomName(String roomName) {
        if (jaasTokenService.isFullyConfigured()) {
            return jaasTokenService.appId() + "/" + roomName;
        }
        return roomName;
    }

    public String issueToken(String roomName, String displayName, boolean moderator) {
        return jaasTokenService.createToken(roomName, displayName, moderator);
    }

    public String buildJoinUrl(
            String roomName, String displayName, boolean audioOnly, boolean moderator, String jwt) {
        String token = jwt != null ? jwt : jaasTokenService.createToken(roomName, displayName, moderator);

        if (jaasTokenService.isFullyConfigured() && token != null) {
            String base = normalizedServerUrl() + "/" + jaasTokenService.appId() + "/" + roomName;
            return base + "?jwt=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        }

        StringBuilder fragment = new StringBuilder();
        fragment.append("config.prejoinPageEnabled=false");
        fragment.append("&config.prejoinConfig.enabled=false");
        fragment.append("&config.requireDisplayName=false");
        fragment.append("&config.enableWelcomePage=false");
        fragment.append("&config.enableLobby=false");
        fragment.append("&config.lobby.enabled=false");
        fragment.append("&config.hideLobbyButton=true");
        fragment.append("&config.enableUserRolesBasedOnToken=false");
        fragment.append("&config.disableDeepLinking=true");
        fragment.append("&config.startWithAudioMuted=false");
        if (audioOnly) {
            fragment.append("&config.startWithVideoMuted=true");
        } else {
            fragment.append("&config.startWithVideoMuted=false");
        }
        if (displayName != null && !displayName.isBlank()) {
            fragment.append("&userInfo.displayName=")
                    .append(URLEncoder.encode(displayName.trim(), StandardCharsets.UTF_8));
        }
        return normalizedServerUrl() + "/" + roomName + "#" + fragment;
    }
}
