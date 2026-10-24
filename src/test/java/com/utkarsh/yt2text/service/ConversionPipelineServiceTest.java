package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.dto.ConvertResponse;
import com.utkarsh.yt2text.dto.TranscriptSegment;
import com.utkarsh.yt2text.exception.CaptionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConversionPipelineServiceTest {

    private UrlValidator urlValidator;
    private CaptionService captionService;
    private TranscriptionService transcriptionService;
    private TranslationService translationService;
    private ExportService exportService;
    private ConversionPipelineService pipelineService;

    @BeforeEach
    void setUp() {
        urlValidator = new UrlValidator();
        captionService = Mockito.mock(CaptionService.class);
        transcriptionService = Mockito.mock(TranscriptionService.class);
        translationService = Mockito.mock(TranslationService.class);
        exportService = new ExportService();

        pipelineService = new ConversionPipelineService(
                urlValidator,
                captionService,
                transcriptionService,
                translationService,
                exportService
        );
    }

    @Test
    @DisplayName("Pipeline branches to captions when captions are present")
    void testPipelineBranchCaptions() {
        String testUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        List<TranscriptSegment> mockSegments = List.of(
                new TranscriptSegment(0.0, 3.0, "Hello world")
        );

        when(captionService.fetchCaptions(eq("https://www.youtube.com/watch?v=dQw4w9WgXcQ"), any()))
                .thenReturn(Optional.of(mockSegments));
        when(translationService.translate("Hello world", "en", "hi")).thenReturn("नमस्ते दुनिया");
        when(translationService.translate("Hello world", "en", "mr")).thenReturn("नमस्कार जग");

        ConvertResponse response = pipelineService.process(testUrl);

        assertTrue(response.success());
        assertNotNull(response.english());
        assertNotNull(response.hindi());
        assertNotNull(response.marathi());

        // Verify fallback transcription was NOT called
        verify(transcriptionService, never()).transcribeAudio(any(), any());

        // Verify safe file retrieval
        byte[] englishBytes = pipelineService.getFileContent(response.english());
        assertEquals("Hello world", new String(englishBytes));
    }

    @Test
    @DisplayName("Pipeline falls back to audio transcription when captions are not present")
    void testPipelineBranchFallbackTranscription() {
        String testUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        List<TranscriptSegment> mockSegments = List.of(
                new TranscriptSegment(0.0, 5.0, "Fallback audio transcription")
        );

        when(captionService.fetchCaptions(eq("https://www.youtube.com/watch?v=dQw4w9WgXcQ"), any()))
                .thenReturn(Optional.empty());
        when(transcriptionService.transcribeAudio(eq("https://www.youtube.com/watch?v=dQw4w9WgXcQ"), any()))
                .thenReturn(mockSegments);
        when(translationService.translate("Fallback audio transcription", "en", "hi")).thenReturn("ऑडियो अनुवाद");
        when(translationService.translate("Fallback audio transcription", "en", "mr")).thenReturn("ऑडिओ भाषांतर");

        ConvertResponse response = pipelineService.process(testUrl);

        assertTrue(response.success());
        verify(transcriptionService, times(1)).transcribeAudio(any(), any());
    }
}

