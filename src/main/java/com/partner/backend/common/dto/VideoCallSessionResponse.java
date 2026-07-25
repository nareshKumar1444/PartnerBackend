package com.partner.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCallSessionResponse {
    private Long appointmentId;
    private String roomName;
    /** Room path for Jitsi External API (includes JaaS app id when configured). */
    private String externalRoomName;
    private String serverUrl;
    private String joinUrl;
    private String displayName;
    private boolean audioOnly;
    /** JaaS JWT — pass to Jitsi External API `jwt` option when embedding. */
    private String jwt;
    private boolean moderator;
    private boolean jaasEnabled;
}
