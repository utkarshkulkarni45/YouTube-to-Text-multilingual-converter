package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.dto.TranscriptSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {

    private ExportService exportService;
    private List<TranscriptSegment> sampleSegments;

    @BeforeEach
    void setUp() {
        exportService = new ExportService();
        sampleSegments = List.of(
                new TranscriptSegment(0.0, 2.5, "Hello world"),
                new TranscriptSegment(2.5, 5.0, "Welcome to the tutorial")
        );
    }

    @Test
    @DisplayName("Should export plain text correctly")
    void testToPlainText() {
        String text = exportService.toPlainText(sampleSegments);
        assertEquals("Hello world\nWelcome to the tutorial", text);
    }

    @Test
    @DisplayName("Should export SRT format with proper numbering and timestamps")
    void testToSrt() {
        String srt = exportService.toSrt(sampleSegments);
        assertTrue(srt.contains("1\n00:00:00,000 --> 00:00:02,500\nHello world"));
        assertTrue(srt.contains("2\n00:00:02,500 --> 00:00:05,000\nWelcome to the tutorial"));
    }

    @Test
    @DisplayName("Should export WebVTT format with WEBVTT header and timestamps")
    void testToVtt() {
        String vtt = exportService.toVtt(sampleSegments);
        assertTrue(vtt.startsWith("WEBVTT"));
        assertTrue(vtt.contains("00:00:00.000 --> 00:00:02.500\nHello world"));
        assertTrue(vtt.contains("00:00:02.500 --> 00:00:05.000\nWelcome to the tutorial"));
    }

    @Test
    @DisplayName("Should export valid JSON string")
    void testToJson() {
        String json = exportService.toJson(sampleSegments);
        assertTrue(json.contains("\"start\" : 0.0"));
        assertTrue(json.contains("\"text\" : \"Hello world\""));
    }
}

