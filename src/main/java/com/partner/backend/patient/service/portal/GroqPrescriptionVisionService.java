package com.partner.backend.patient.service.portal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.partner.backend.patient.dto.portal.PrescriptionMedicationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prescription slip OCR via Groq Vision (patient scan only).
 * CNIC OCR continues to use {@link com.partner.backend.common.service.CnicImageOcrService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroqPrescriptionVisionService {

    private static final String PROMPT = """
            Analyze this prescription image.

            Extract all medicines and return ONLY valid JSON.

            {
              "medications": [
                {
                  "name": "",
                  "strength": "",
                  "dosage": "",
                  "duration": "",
                  "quantity": ""
                }
              ]
            }

            Rules:
            1. Extract complete medicine names.
            2. Extract strength such as 250mg, 500mg, 1g, etc.
            3. Extract dosage instructions if visible.
            4. Calculate total quantity required if dosage and duration are available.
            5. If a field cannot be determined, use "Unknown".
            6. Return ONLY JSON. No explanations.
            """;

    private static final Pattern CODE_FENCE_PATTERN =
            Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```", Pattern.CASE_INSENSITIVE);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final ObjectMapper objectMapper;

    @Value("${app.groq.api-key:}")
    private String apiKey;

    @Value("${app.groq.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqUrl;

    @Value("${app.groq.vision-model:meta-llama/llama-4-scout-17b-16e-instruct}")
    private String model;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Optional<List<PrescriptionMedicationDto>> extractMedications(byte[] imageBytes, String filename) {
        if (!isConfigured() || imageBytes == null || imageBytes.length == 0) {
            return Optional.empty();
        }
        try {
            String mime = resolveMimeType(imageBytes, filename);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String requestBody = buildRequestBody(base64, mime);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(groqUrl))
                    .timeout(Duration.ofSeconds(90))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Groq vision HTTP {} body={}", response.statusCode(), truncate(response.body(), 400));
                return Optional.empty();
            }
            if (response.body() == null || response.body().isBlank()) {
                return Optional.empty();
            }

            JsonNode body = objectMapper.readTree(response.body());
            String assistantText = body.path("choices").path(0).path("message").path("content").asText(null);
            if (assistantText == null || assistantText.isBlank()) {
                log.warn("Groq vision returned empty assistant content");
                return Optional.empty();
            }

            List<PrescriptionMedicationDto> medications = parseMedicationsJson(assistantText);
            if (medications.isEmpty()) {
                log.warn("Groq vision parsed zero medications from: {}", truncate(assistantText, 400));
                return Optional.empty();
            }
            log.info("Groq vision extracted {} medication(s)", medications.size());
            return Optional.of(medications);
        } catch (Exception ex) {
            log.warn("Groq prescription vision failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String buildRequestBody(String base64Image, String mime) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        ArrayNode messages = root.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        ArrayNode content = userMessage.putArray("content");
        content.addObject().put("type", "text").put("text", PROMPT);
        content.addObject()
                .put("type", "image_url")
                .set("image_url", objectMapper.createObjectNode()
                        .put("url", "data:" + mime + ";base64," + base64Image));
        return objectMapper.writeValueAsString(root);
    }

    static String buildRawTextFromMedications(List<PrescriptionMedicationDto> medications) {
        StringBuilder sb = new StringBuilder();
        for (PrescriptionMedicationDto med : medications) {
            if (med == null || !isUsableField(med.getName())) {
                continue;
            }
            sb.append(med.getName().trim());
            if (isUsableField(med.getStrength())) {
                sb.append(" ").append(med.getStrength().trim());
            }
            if (isUsableField(med.getDosage())) {
                sb.append(" ").append(med.getDosage().trim());
            }
            if (isUsableField(med.getQuantity())) {
                sb.append(" Qty ").append(med.getQuantity().trim());
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private List<PrescriptionMedicationDto> parseMedicationsJson(String assistantText) {
        String json = extractJsonPayload(assistantText.trim());
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode medsNode = root.path("medications");
            if (!medsNode.isArray()) {
                return List.of();
            }
            List<PrescriptionMedicationDto> results = new ArrayList<>();
            for (JsonNode node : medsNode) {
                String name = textField(node, "name");
                if (!isUsableField(name)) {
                    continue;
                }
                results.add(PrescriptionMedicationDto.builder()
                        .name(name)
                        .strength(textField(node, "strength"))
                        .dosage(textField(node, "dosage"))
                        .duration(textField(node, "duration"))
                        .quantity(textField(node, "quantity"))
                        .build());
            }
            return results;
        } catch (Exception ex) {
            log.warn("Unable to parse Groq medications JSON: {}", ex.getMessage());
            return List.of();
        }
    }

    private static String extractJsonPayload(String text) {
        Matcher fence = CODE_FENCE_PATTERN.matcher(text);
        if (fence.find()) {
            return fence.group(1).trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private static String textField(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText("").trim();
        return text.isEmpty() ? null : text;
    }

    private static boolean isUsableField(String value) {
        return value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value.trim());
    }

    private static String resolveMimeType(byte[] imageBytes, String filename) {
        if (imageBytes != null && imageBytes.length >= 4) {
            if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == 0x50 && imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
                return "image/png";
            }
            if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
                return "image/jpeg";
            }
            if (imageBytes.length >= 12
                    && imageBytes[0] == 'R' && imageBytes[1] == 'I' && imageBytes[2] == 'F' && imageBytes[3] == 'F'
                    && imageBytes[8] == 'W' && imageBytes[9] == 'E' && imageBytes[10] == 'B' && imageBytes[11] == 'P') {
                return "image/webp";
            }
            if (imageBytes[0] == 'G' && imageBytes[1] == 'I' && imageBytes[2] == 'F') {
                return "image/gif";
            }
        }
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}
