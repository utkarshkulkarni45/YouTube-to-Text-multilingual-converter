package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.client.YtDlpClient;
import com.utkarsh.yt2text.dto.TranscriptSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CaptionServiceTest {

    private YtDlpClient ytDlpClient;
    private CaptionService captionService;

    @BeforeEach
    void setUp() {
        ytDlpClient = Mockito.mock(YtDlpClient.class);
        captionService = new CaptionService(ytDlpClient);
    }

    @Test
    @DisplayName("Should return empty optional when no subtitles are downloaded")
    void testNoSubtitlesFound(@TempDir Path tempDir) {
        when(ytDlpClient.downloadSubtitles(any(), eq(tempDir))).thenReturn(Optional.empty());

        Optional<List<TranscriptSegment>> result = captionService.fetchCaptions("https://www.youtube.com/watch?v=dQw4w9WgXcQ", tempDir);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should parse WebVTT file correctly")
    void testParseVtt(@TempDir Path tempDir) throws IOException {
        Path vttFile = tempDir.resolve("caption.vtt");
        String vttContent = """
                WEBVTT
                Kind: captions
                Language: en

                00:00:01.000 --> 00:00:04.000
                Never gonna give you up

                00:00:04.500 --> 00:00:07.000
                Never gonna let you down
                """;
        Files.writeString(vttFile, vttContent);

        when(ytDlpClient.downloadSubtitles(any(), eq(tempDir))).thenReturn(Optional.of(vttFile.toFile()));

        Optional<List<TranscriptSegment>> result = captionService.fetchCaptions("https://www.youtube.com/watch?v=dQw4w9WgXcQ", tempDir);
        assertTrue(result.isPresent());
        List<TranscriptSegment> segments = result.get();
        assertEquals(2, segments.size());
        assertEquals("Never gonna give you up", segments.get(0).text());
        assertEquals("Never gonna let you down", segments.get(1).text());
    }
}

