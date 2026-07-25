package com.partner.backend.common.service;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Optional 8x8 JaaS tokens (free dev tier at https://jaas.8x8.vc/).
 * Local dev: place private key at jaas-keys/jwt.key and kid at jaas-keys/kid.txt after uploading jwt.key.pub.pem.
 */
@Service
@Slf4j
public class JitsiJaasTokenService {

    @Value("${app.jitsi.jaas-app-id:}")
    private String jaasAppId;

    @Value("${app.jitsi.jaas-kid:}")
    private String jaasKeyId;

    @Value("${app.jitsi.jaas-private-key-pem:}")
    private String privateKeyPem;

    @Value("${app.jitsi.jaas-private-key-file:jaas-keys/jwt.key}")
    private String privateKeyFile;

    @Value("${app.jitsi.jaas-kid-file:jaas-keys/kid.txt}")
    private String kidFile;

    private PrivateKey privateKey;
    private String resolvedKid;
    private boolean enabled;

    @PostConstruct
    void init() {
        String pem = resolvePrivateKeyPem();
        resolvedKid = resolveKid();

        enabled = jaasAppId != null && !jaasAppId.isBlank()
                && pem != null && !pem.isBlank();
        if (!enabled) {
            log.warn("""
                    Jitsi JaaS is NOT configured — video calls use public meet.jit.si which may show \
                    "waiting for moderator". Set app.jitsi.jaas-app-id and jaas-keys/jwt.key (see jaas-keys/README.txt).""");
            return;
        }
        try {
            privateKey = loadPrivateKey(pem);
            if (resolvedKid == null || resolvedKid.isBlank()) {
                log.warn("""
                        Jitsi JaaS private key loaded but kid is missing. Upload jaas-keys/jwt.key.pub.pem at \
                        https://jaas.8x8.vc/#/apikeys then paste the Key ID into jaas-keys/kid.txt and restart.""");
            } else {
                log.info("Jitsi JaaS enabled for app id {} (kid configured)", jaasAppId.trim());
            }
        } catch (Exception e) {
            enabled = false;
            log.error("Failed to load Jitsi JaaS private key — video calls will use meet.jit.si fallback", e);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isFullyConfigured() {
        return enabled && resolvedKid != null && !resolvedKid.isBlank();
    }

    public String appId() {
        return jaasAppId == null ? "" : jaasAppId.trim();
    }

    public String createToken(String roomName, String displayName, boolean moderator) {
        if (!isFullyConfigured()) {
            return null;
        }
        String name = displayName == null || displayName.isBlank() ? "Guest" : displayName.trim();

        Map<String, Object> user = new HashMap<>();
        user.put("id", java.util.UUID.randomUUID().toString());
        user.put("name", name);
        user.put("email", name.replaceAll("\\s+", ".").toLowerCase() + "@healthportalhub.local");
        user.put("avatar", "");
        user.put("moderator", moderator ? "true" : "false");

        Map<String, Object> features = new HashMap<>();
        features.put("recording", false);
        features.put("livestreaming", false);
        features.put("transcription", false);
        features.put("outbound-call", false);

        Map<String, Object> context = new HashMap<>();
        context.put("user", user);
        context.put("features", features);

        Instant now = Instant.now();

        return Jwts.builder()
                .header()
                .keyId(resolvedKid.trim())
                .type("JWT")
                .and()
                .issuer("chat")
                .claim("aud", "jitsi")
                .subject(appId())
                .claim("room", "*")
                .claim("context", context)
                .issuedAt(java.util.Date.from(now))
                .notBefore(java.util.Date.from(now.minusSeconds(10)))
                .expiration(java.util.Date.from(now.plusSeconds(7200)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private String resolvePrivateKeyPem() {
        if (privateKeyPem != null && !privateKeyPem.isBlank()) {
            return privateKeyPem;
        }
        if (privateKeyFile == null || privateKeyFile.isBlank()) {
            return "";
        }
        try {
            Path path = Path.of(privateKeyFile.trim());
            if (!path.isAbsolute()) {
                path = Path.of(System.getProperty("user.dir")).resolve(path).normalize();
            }
            if (Files.isRegularFile(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
            log.warn("Jitsi JaaS private key file not found: {}", path);
        } catch (Exception e) {
            log.warn("Could not read Jitsi JaaS private key file: {}", e.getMessage());
        }
        return "";
    }

    private String resolveKid() {
        if (jaasKeyId != null && !jaasKeyId.isBlank()) {
            return jaasKeyId.trim();
        }
        if (kidFile == null || kidFile.isBlank()) {
            return "";
        }
        try {
            Path path = Path.of(kidFile.trim());
            if (!path.isAbsolute()) {
                path = Path.of(System.getProperty("user.dir")).resolve(path).normalize();
            }
            if (Files.isRegularFile(path)) {
                String kid = Files.readString(path, StandardCharsets.UTF_8).trim();
                if (!kid.isEmpty() && !kid.startsWith("#")) {
                    return kid;
                }
            }
        } catch (Exception e) {
            log.warn("Could not read Jitsi JaaS kid file: {}", e.getMessage());
        }
        return "";
    }

    private static PrivateKey loadPrivateKey(String pem) throws Exception {
        String normalized = pem
                .replace("\\n", "\n")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }
}
