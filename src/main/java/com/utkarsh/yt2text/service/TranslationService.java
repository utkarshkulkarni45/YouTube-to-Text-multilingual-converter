package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.client.TranslationProvider;
import com.utkarsh.yt2text.config.AppProperties;
import com.utkarsh.yt2text.exception.TranslationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);
    private final Map<String, TranslationProvider> providers = new ConcurrentHashMap<>();
    private final AppProperties appProperties;

    public TranslationService(List<TranslationProvider> providerList, AppProperties appProperties) {
        this.appProperties = appProperties;
        for (TranslationProvider provider : providerList) {
            providers.put(provider.getName().toLowerCase(), provider);
        }
    }

    public String translate(String text, String sourceLang, String targetLang) {
        String providerName = appProperties.getTranslation().getProvider().toLowerCase();
        TranslationProvider provider = providers.get(providerName);
        if (provider == null) {
            // Default to first available or mymemory
            provider = providers.values().stream().findFirst()
                    .orElseThrow(() -> new TranslationException("No translation provider available."));
        }

        log.info("Translating text ({} chars) from {} to {} using provider '{}'",
                text != null ? text.length() : 0, sourceLang, targetLang, provider.getName());
        return provider.translate(text, sourceLang, targetLang);
    }
}

