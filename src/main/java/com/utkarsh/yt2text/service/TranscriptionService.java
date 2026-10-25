package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.client.WhisperClient;
import com.utkarsh.yt2text.client.YtDlpClient;
import com.utkarsh.yt2text.dto.TranscriptSegment;
import com.utkarsh.yt2text.exception.TranscriptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Service
public class TranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionService.class);
    private final YtDlpClient ytDlpClient;
    private final WhisperClient whisperClient;

    public TranscriptionService(YtDlpClient ytDlpClient, WhisperClient whisperClient) {
        this.ytDlpClient = ytDlpClient;
        this.whisperClient = whisperClient;
    }

    /**
     * Fallback speech-to-text when no captions exist:
     * Downloads audio via yt-dlp and transcribes via Whisper API client.
     */
    public List<TranscriptSegment> transcribeAudio(String canonicalUrl, Path workDir) {
        log.info("Captions not found. Starting audio download fallback for: {}", canonicalUrl);
        File audioFile = ytDlpClient.downloadAudio(canonicalUrl, workDir);

        if (audioFile == null || !audioFile.exists() || audioFile.length() == 0) {
            throw new TranscriptionException("Audio download failed or generated empty file.");
        }

        log.info("Sending audio ({} bytes) to Whisper API for transcription", audioFile.length());
        String fullText = whisperClient.transcribe(audioFile);

        if (fullText == null || fullText.isBlank()) {
            throw new TranscriptionException("No text was transcribed from the audio.");
        }

        // Return as single transcript segment spanning whole audio
        return List.of(new TranscriptSegment(0.0, 0.0, fullText.trim()));
    }
}

