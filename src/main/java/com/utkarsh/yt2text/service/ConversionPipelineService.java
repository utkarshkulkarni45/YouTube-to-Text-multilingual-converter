package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.dto.ConvertResponse;
import com.utkarsh.yt2text.dto.TranscriptSegment;
import com.utkarsh.yt2text.exception.CaptionNotFoundException;
import com.utkarsh.yt2text.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversionPipelineService {

    private static final Logger log = LoggerFactory.getLogger(ConversionPipelineService.class);

    private final UrlValidator urlValidator;
    private final CaptionService captionService;
    private final TranscriptionService transcriptionService;
    private final TranslationService translationService;
    private final ExportService exportService;

    // In-memory safe cache for generated file downloads to prevent path traversal
    private final Map<String, byte[]> downloadCache = new ConcurrentHashMap<>();

    public ConversionPipelineService(
            UrlValidator urlValidator,
            CaptionService captionService,
            TranscriptionService transcriptionService,
            TranslationService translationService,
            ExportService exportService
    ) {
        this.urlValidator = urlValidator;
        this.captionService = captionService;
        this.transcriptionService = transcriptionService;
        this.translationService = translationService;
        this.exportService = exportService;
    }

    /**
     * Executes the conversion pipeline:
     * 1. Validate URL & canonicalize
     * 2. Attempt fetching captions via yt-dlp
     * 3. Fallback to audio download + Whisper API if captions are missing
     * 4. Translate English text into Hindi and Marathi
     * 5. Cache outputs for safe file retrieval
     */
    public ConvertResponse process(String rawUrl) {
        String videoId = urlValidator.extractVideoId(rawUrl);
        String canonicalUrl = urlValidator.buildCanonicalUrl(videoId);

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("yt2text-" + UUID.randomUUID());

            // 1. Try captions first
            Optional<List<TranscriptSegment>> segmentsOpt = captionService.fetchCaptions(canonicalUrl, tempDir);

            // 2. Fallback to audio download + Whisper speech-to-text
            List<TranscriptSegment> segments;
            if (segmentsOpt.isPresent()) {
                segments = segmentsOpt.get();
                log.info("Captions successfully retrieved for video {}", videoId);
            } else {
                segments = transcriptionService.transcribeAudio(canonicalUrl, tempDir);
                log.info("Audio fallback transcribed successfully for video {}", videoId);
            }

            if (segments.isEmpty()) {
                throw new CaptionNotFoundException("No text was transcribed from the audio.");
            }

            // 3. Format English transcript
            String englishText = exportService.toPlainText(segments);
            if (englishText.isBlank()) {
                throw new CaptionNotFoundException("No text was transcribed from the audio.");
            }

            // 4. Translate to Hindi and Marathi
            String hindiText = translationService.translate(englishText, "en", "hi");
            String marathiText = translationService.translate(englishText, "en", "mr");

            // 5. Store files safely in download cache
            String reqId = UUID.randomUUID().toString().substring(0, 8);
            String englishFilename = reqId + "_english_transcript.txt";
            String hindiFilename = reqId + "_hindi_transcript.txt";
            String marathiFilename = reqId + "_marathi_transcript.txt";

            downloadCache.put(englishFilename, englishText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            downloadCache.put(hindiFilename, hindiText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            downloadCache.put(marathiFilename, marathiText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return new ConvertResponse(true, englishFilename, hindiFilename, marathiFilename);
        } catch (IOException e) {
            throw new RuntimeException("Temporary storage error: " + e.getMessage(), e);
        } finally {
            // Guarantee all temporary files created on disk are deleted
            if (tempDir != null) {
                deleteDirectoryRecursively(tempDir);
            }
        }
    }

    public byte[] getFileContent(String filename) {
        // Enforce safe filename check
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResourceNotFoundException("File not found");
        }
        byte[] content = downloadCache.get(filename);
        if (content == null) {
            throw new ResourceNotFoundException("File not found");
        }
        return content;
    }

    private void deleteDirectoryRecursively(Path dir) {
        try {
            if (Files.exists(dir)) {
                try (var stream = Files.walk(dir)) {
                    stream.sorted(Comparator.reverseOrder())
                          .forEach(path -> {
                              try {
                                  Files.deleteIfExists(path);
                              } catch (IOException ignored) {}
                          });
                }
            }
        } catch (Exception e) {
            log.warn("Failed to clean up temp directory {}: {}", dir, e.getMessage());
        }
    }
}

