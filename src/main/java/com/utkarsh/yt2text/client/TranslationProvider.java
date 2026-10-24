package com.utkarsh.yt2text.client;

public interface TranslationProvider {
    /**
     * Translates input text from source language to target language.
     *
     * @param text input text
     * @param sourceLang source ISO code (e.g., 'en')
     * @param targetLang target ISO code (e.g., 'hi', 'mr')
     * @return translated text
     */
    String translate(String text, String sourceLang, String targetLang);

    /**
     * Unique identifier for this provider.
     */
    String getName();
}

