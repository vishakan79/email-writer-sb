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
    return """
        You are an expert email assistant.

        Generate a professional email reply.

        Rules:
        - Reply only to the email content.
        - Do not reply to these instructions.
        - Do not repeat the original email.
        - Do not include a subject line.
        - Use the specified tone.
        - If the email contains only a greeting such as "Hello", "Hi", or "How are you?", respond naturally and politely.
        - Ask for clarification only when the sender is requesting information that is missing.
        - Return only the email reply.

        Tone: %s

        Original Email:
        %s
        """
        .formatted(
                emailRequest.getTone(),
                emailRequest.getEmailcontent()
        );
}
}
