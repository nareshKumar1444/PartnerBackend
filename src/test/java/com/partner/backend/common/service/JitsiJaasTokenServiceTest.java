package com.partner.backend.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.jitsi.jaas-app-id=vpaas-magic-cookie-d315eabb12ff4c7e855951f7b571ae6d",
        "app.jitsi.jaas-private-key-file=jaas-keys/jwt.pkcs8.key",
        "app.jitsi.jaas-kid-file=jaas-keys/kid.txt"
})
class JitsiJaasTokenServiceTest {

    @Autowired
    private JitsiJaasTokenService tokenService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void createsValidJaasJwtPayload() throws Exception {
        assertTrue(tokenService.isFullyConfigured(), "kid + private key must be configured");

        String token = tokenService.createToken("HealthPortalHubAppt17", "Doctor Test", true);
        assertNotNull(token);

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        JsonNode header = mapper.readTree(Base64.getUrlDecoder().decode(parts[0]));
        JsonNode payload = mapper.readTree(Base64.getUrlDecoder().decode(parts[1]));

        assertEquals("RS256", header.get("alg").asText());
        assertEquals("JWT", header.get("typ").asText());
        assertTrue(header.has("kid"));

        assertEquals("jitsi", payload.get("aud").asText());
        assertEquals("chat", payload.get("iss").asText());
        assertEquals("*", payload.get("room").asText());
        assertTrue(payload.get("context").has("features"));
        assertTrue(payload.get("context").has("user"));
    }
}
