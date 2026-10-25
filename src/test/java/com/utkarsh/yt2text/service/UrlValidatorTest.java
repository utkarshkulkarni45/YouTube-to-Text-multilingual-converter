package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.exception.InvalidUrlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    private UrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UrlValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "http://youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ",
            "www.youtube.com/watch?v=dQw4w9WgXcQ&t=10s",
            "https://www.youtube.com/watch?feature=share&v=dQw4w9WgXcQ"
    })
    @DisplayName("Should correctly extract 11-char video ID from valid YouTube URLs")
    void shouldExtractVideoIdFromValidUrls(String url) {
        String videoId = validator.extractVideoId(url);
        assertEquals("dQw4w9WgXcQ", videoId);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "https://vimeo.com/123456789",
            "https://google.com",
            "https://www.youtube.com/watch?v=short",
            "https://www.youtube.com/watch?v=12345678901234",
            "https://malicious.com/fake?v=dQw4w9WgXcQ"
    })
    @DisplayName("Should throw InvalidUrlException for invalid or non-YouTube URLs")
    void shouldRejectInvalidUrls(String url) {
        assertThrows(InvalidUrlException.class, () -> validator.extractVideoId(url));
    }

    @Test
    @DisplayName("Should construct secure canonical URL")
    void shouldBuildCanonicalUrl() {
        String canonical = validator.buildCanonicalUrl("dQw4w9WgXcQ");
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", canonical);
    }
}

