package com.utkarsh.yt2text.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utkarsh.yt2text.config.AppProperties;
import com.utkarsh.yt2text.exception.TranscriptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.File;

@Component
public class HttpWhisperClient implements WhisperClient {

    private static final Logger log = LoggerFactory.getLogger(HttpWhisperClient.class);
    private final RestClient restClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public HttpWhisperClient(RestClient restClient, AppProperties appProperties) {
        this.restClient = restClient;
        this.appProperties = appProperties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String transcribe(File audioFile) {
        String apiKey = appProperties.getWhisper().getApiKey();
        String apiUrl = appProperties.getWhisper().getApiUrl();
        String model = appProperties.getWhisper().getModel();

        if (apiKey == null || apiKey.isBlank()) {
            throw new TranscriptionException("Whisper API key is not configured (set OPENAI_API_KEY environment variable).");
        }

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(audioFile));
            body.add("model", model);

            String response = restClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            if (root.has("text")) {
                return root.get("text").asText();
            } else {
                throw new TranscriptionException("Whisper response did not contain text field: " + response);
            }
        } catch (TranscriptionException te) {
            throw te;
        } catch (Exception e) {
            log.error("Failed to transcribe audio via Whisper API", e);
            throw new TranscriptionException("Whisper transcription failed: " + e.getMessage(), e);
        }
    }
}

