package com.utkarsh.yt2text.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.utkarsh.yt2text.dto.TranscriptSegment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ExportService {

    private final ObjectMapper objectMapper;

    public ExportService() {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Formats segments as plain text (.txt).
     */
    public String toPlainText(List<TranscriptSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (TranscriptSegment segment : segments) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(segment.text());
        }
        return sb.toString();
    }

    /**
     * Formats segments as SubRip Subtitles (.srt).
     */
    public String toSrt(List<TranscriptSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (TranscriptSegment segment : segments) {
            sb.append(counter++).append("\n");
            sb.append(formatSrtTime(segment.start()))
              .append(" --> ")
              .append(formatSrtTime(segment.end()))
              .append("\n");
            sb.append(segment.text()).append("\n\n");
        }
        return sb.toString().trim();
    }

    /**
     * Formats segments as WebVTT (.vtt).
     */
    public String toVtt(List<TranscriptSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            return "WEBVTT\n";
        }
        StringBuilder sb = new StringBuilder("WEBVTT\n\n");
        for (TranscriptSegment segment : segments) {
            sb.append(formatVttTime(segment.start()))
              .append(" --> ")
              .append(formatVttTime(segment.end()))
              .append("\n");
            sb.append(segment.text()).append("\n\n");
        }
        return sb.toString().trim();
    }

    /**
     * Formats segments as structured JSON.
     */
    public String toJson(List<TranscriptSegment> segments) {
        try {
            return objectMapper.writeValueAsString(segments);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String formatSrtTime(double totalSeconds) {
        long hours = (long) (totalSeconds / 3600);
        long minutes = (long) ((totalSeconds % 3600) / 60);
        long seconds = (long) (totalSeconds % 60);
        long millis = (long) Math.round((totalSeconds - (long) totalSeconds) * 1000);
        return String.format(Locale.ROOT, "%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
    }

    private String formatVttTime(double totalSeconds) {
        long hours = (long) (totalSeconds / 3600);
        long minutes = (long) ((totalSeconds % 3600) / 60);
        long seconds = (long) (totalSeconds % 60);
        long millis = (long) Math.round((totalSeconds - (long) totalSeconds) * 1000);
        return String.format(Locale.ROOT, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }
}

