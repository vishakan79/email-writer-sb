package com.email.email_writer_sb.writerapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGenertorService {

    private final WebClient webClient;
    private final String geminiUrl;
    private final String geminiApiKey;

    // ✅ ONLY THIS CONSTRUCTOR (CORRECT)
    public EmailGenertorService(
            WebClient.Builder webClientBuilder,
            @Value("${GEMINI_URL}") String geminiUrl,
            @Value("${GEMINI_KEY}") String geminiApiKey
    ) {
        this.webClient = webClientBuilder.build();
        this.geminiUrl = geminiUrl;
        this.geminiApiKey = geminiApiKey;
    }

    public String generateEmailReply(EmailRequest emailRequest) {

        String prompt = buildPrompt(emailRequest);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        String response = webClient.post()
                .uri(geminiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);

            return rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            return "Error processing response: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are an expert email assistant.
                
                Write a clear, polite, and well-structured email reply.
                Follow professional email standards:
                - Proper greeting
                - Clear body
                - Polite closing
                - Sign-off
                
                Do NOT include a subject line.
                Do NOT repeat the original email.
                """);

        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.\n");
        }

        prompt.append("\nOriginal email:\n");
        prompt.append(emailRequest.getEmailcontent());
        prompt.append("\n\nWrite the reply now.");

        return prompt.toString();
    }
}
