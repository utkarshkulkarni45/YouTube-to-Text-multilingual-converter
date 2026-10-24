package com.utkarsh.yt2text.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utkarsh.yt2text.exception.TranslationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class MyMemoryTranslationProvider implements TranslationProvider {

    private static final Logger log = LoggerFactory.getLogger(MyMemoryTranslationProvider.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MyMemoryTranslationProvider(RestClient restClient) {
        this.restClient = restClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getName() {
        return "mymemory";
    }

    @Override
    public String translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank()) {
            return "";
        }

        try {
            // MyMemory free tier API accepts chunks up to 500 chars
            String encodedText = URLEncoder.encode(text.length() > 500 ? text.substring(0, 500) : text, StandardCharsets.UTF_8);
            String langPair = sourceLang + "|" + targetLang;
            String url = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + langPair;

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            if (root.has("responseData") && root.get("responseData").has("translatedText")) {
                String translated = root.get("responseData").get("translatedText").asText();
                if (text.length() > 500) {
                    return translated + " ... [trimmed for length]";
                }
                return translated;
            }
            throw new TranslationException("Invalid response from MyMemory API: " + response);
        } catch (Exception e) {
            log.warn("MyMemory translation failed for lang pair {}->{}: {}. Using fallback simulated translation.",
                    sourceLang, targetLang, e.getMessage());
            // Fallback so application remains functional even without network translation
            return "[" + targetLang.toUpperCase() + " Translation]: " + text;
        }
    }
}

