package com.partner.backend.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.partner.backend.common.util.CnicTextParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Optional;

/**
 * Optional OCR via OCR.space (configure {@code app.ocr-space.api-key}).
 * Disabled when key blank — client must collect CNIC manually.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CnicImageOcrService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.ocr-space.api-key:}")
    private String apiKey;

    @Value("${app.ocr-space.url:https://api.ocr.space/parse/image}")
    private String ocrUrl;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Returns full OCR text from an image when OCR.space is configured. */
    public Optional<String> extractPlainText(byte[] imageBytes, String filename) {
        if (!isConfigured() || imageBytes == null || imageBytes.length == 0) {
            return Optional.empty();
        }
        return requestOcrParsedText(imageBytes, filename);
    }

    /** Returns normalized 13-digit CNIC when OCR finds a plausible match. */
    public Optional<String> extractNormalizedCnic(byte[] imageBytes, String filename) {
        return extractPlainText(imageBytes, filename)
                .map(CnicTextParser::findFirstNormalized)
                .flatMap(cnic -> cnic != null ? Optional.of(cnic) : Optional.empty());
    }

    private Optional<String> requestOcrParsedText(byte[] imageBytes, String filename) {
        String resolvedFilename = resolveUploadFilename(imageBytes, filename);
        String ocrFileType = toOcrFileType(resolvedFilename);

        Optional<String> primary = callOcrSpace(imageBytes, resolvedFilename, ocrFileType, "2");
        if (primary.isPresent()) {
            return primary;
        }
        return callOcrSpace(imageBytes, resolvedFilename, ocrFileType, "1");
    }

    private Optional<String> callOcrSpace(
            byte[] imageBytes,
            String resolvedFilename,
            String ocrFileType,
            String ocrEngine
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("apikey", apiKey.trim());

        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return resolvedFilename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("language", "eng");
        body.add("OCREngine", ocrEngine);
        body.add("isOverlayRequired", "false");
        body.add("scale", "true");
        body.add("detectOrientation", "true");
        body.add("filetype", ocrFileType);

        try {
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
            String json = restTemplate.postForObject(ocrUrl, entity, String.class);
            if (json == null) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(json);
            if (root.path("IsErroredOnProcessing").asBoolean(false)) {
                log.warn("OCR.space processing error (engine {}): {}", ocrEngine, root.path("ErrorMessage"));
                return Optional.empty();
            }
            if (root.hasNonNull("ParsedResults") && root.get("ParsedResults").isArray()
                    && !root.get("ParsedResults").isEmpty()) {
                JsonNode first = root.get("ParsedResults").get(0);
                if (first != null && first.hasNonNull("ParsedText")) {
                    String text = first.get("ParsedText").asText("").trim();
                    if (!text.isEmpty()) {
                        return Optional.of(text);
                    }
                }
                if (first != null && first.hasNonNull("ErrorMessage")) {
                    log.warn("OCR.space parse error (engine {}): {}", ocrEngine, first.get("ErrorMessage"));
                }
            }
            if (root.hasNonNull("ErrorMessage")) {
                log.warn("OCR.space error payload (engine {}): {}", ocrEngine, root.get("ErrorMessage"));
            }
        } catch (Exception ex) {
            log.warn("OCR extraction failed (engine {}): {}", ocrEngine, ex.getMessage());
        }
        return Optional.empty();
    }

    /** OCR.space rejects uploads without a recognizable extension unless filetype is set. */
    static String resolveUploadFilename(byte[] imageBytes, String filename) {
        String trimmed = filename != null ? filename.trim() : "";
        if (!trimmed.isEmpty() && trimmed.contains(".")) {
            String ext = trimmed.substring(trimmed.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (isKnownImageExtension(ext) && !ext.isBlank()) {
                return trimmed;
            }
        }
        String baseName = "scan";
        if (!trimmed.isEmpty()) {
            int dot = trimmed.indexOf('.');
            baseName = (dot > 0 ? trimmed.substring(0, dot) : trimmed).replaceAll("[^a-zA-Z0-9._-]", "");
            if (baseName.isBlank()) {
                baseName = "scan";
            }
        }
        return baseName + "." + detectExtensionFromBytes(imageBytes);
    }

    static String detectExtensionFromBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return "jpg";
        }
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return "png";
        }
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return "jpg";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "webp";
        }
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
            return "gif";
        }
        return "jpg";
    }

    static String toOcrFileType(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot >= filename.length() - 1) {
            return "JPG";
        }
        return switch (filename.substring(dot + 1).toLowerCase(Locale.ROOT)) {
            case "png" -> "PNG";
            case "gif" -> "GIF";
            case "webp" -> "WEBP";
            case "bmp" -> "BMP";
            case "tif", "tiff" -> "TIF";
            default -> "JPG";
        };
    }

    private static boolean isKnownImageExtension(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "webp", "bmp", "tif", "tiff", "heic", "heif" -> true;
            default -> false;
        };
    }
}
