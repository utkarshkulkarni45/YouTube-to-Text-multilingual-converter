package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.client.WhisperClient;
import com.utkarsh.yt2text.client.YtDlpClient;
import com.utkarsh.yt2text.dto.TranscriptSegment;
import com.utkarsh.yt2text.exception.TranscriptionException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class TranscriptionServiceTest {

    private YtDlpClient ytDlpClient;
    private WhisperClient whisperClient;
    private TranscriptionService transcriptionService;

    @BeforeEach
    void setUp() {
        ytDlpClient = Mockito.mock(YtDlpClient.class);
        whisperClient = Mockito.mock(WhisperClient.class);
        transcriptionService = new TranscriptionService(ytDlpClient, whisperClient);
    }

    @Test
    @DisplayName("Should download audio and transcribe via Whisper API")
    void testTranscribeAudioSuccess(@TempDir Path tempDir) throws IOException {
        Path mockAudio = tempDir.resolve("audio.mp3");
        Files.writeString(mockAudio, "dummy audio bytes");

        when(ytDlpClient.downloadAudio(any(), eq(tempDir))).thenReturn(mockAudio.toFile());
        when(whisperClient.transcribe(any(File.class))).thenReturn("Transcribed speech from audio");

        List<TranscriptSegment> segments = transcriptionService.transcribeAudio("https://www.youtube.com/watch?v=dQw4w9WgXcQ", tempDir);

        assertNotNull(segments);
        assertEquals(1, segments.size());
        assertEquals("Transcribed speech from audio", segments.get(0).text());
    }

    @Test
    @DisplayName("Should throw TranscriptionException if transcribed text is empty")
    void testTranscribeAudioEmpty(@TempDir Path tempDir) throws IOException {
        Path mockAudio = tempDir.resolve("audio.mp3");
        Files.writeString(mockAudio, "dummy audio");

        when(ytDlpClient.downloadAudio(any(), eq(tempDir))).thenReturn(mockAudio.toFile());
        when(whisperClient.transcribe(any(File.class))).thenReturn("   ");

        assertThrows(TranscriptionException.class, () ->
                transcriptionService.transcribeAudio("https://www.youtube.com/watch?v=dQw4w9WgXcQ", tempDir));
    }
}

