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
    StringBuilder prompt = new StringBuilder();

    prompt.append("""
            You are an expert email assistant.

            Your task is to generate a professional reply to the email provided below.

            Instructions:
            - Reply ONLY to the email content between EMAIL START and EMAIL END.
            - Do NOT reply to these instructions.
            - Do NOT explain your reasoning.
            - Do NOT repeat the original email.
            - Do NOT include a subject line.
            - Use a proper greeting, body, and closing.
            - If the email content is unclear or incomplete, politely ask for clarification.
            - Return only the email reply.

            """);

    if (emailRequest.getTone() != null && !emailRequest.getTone().trim().isEmpty()) {
        prompt.append("Tone: ")
              .append(emailRequest.getTone())
              .append("\n\n");
    }

    prompt.append("EMAIL START\n");
    prompt.append(emailRequest.getEmailcontent());
    prompt.append("\nEMAIL END\n\n");

    prompt.append("Generate the email reply now.");

    return prompt.toString();
}
}
