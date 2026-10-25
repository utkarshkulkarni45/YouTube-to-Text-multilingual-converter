package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.client.YtDlpClient;
import com.utkarsh.yt2text.dto.TranscriptSegment;
import com.utkarsh.yt2text.exception.CaptionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CaptionService {

    private static final Logger log = LoggerFactory.getLogger(CaptionService.class);
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile(
            "([0-9]{2}:)?([0-9]{2}):([0-9]{2})\\.[0-9]{3} --> ([0-9]{2}:)?([0-9]{2}):([0-9]{2})\\.[0-9]{3}"
    );

    private final YtDlpClient ytDlpClient;

    public CaptionService(YtDlpClient ytDlpClient) {
        this.ytDlpClient = ytDlpClient;
    }

    /**
     * Attempts to fetch existing captions using yt-dlp.
     *
     * @param canonicalUrl validated YouTube URL
     * @param workDir temporary working directory
     * @return parsed transcript segments if captions found
     */
    public Optional<List<TranscriptSegment>> fetchCaptions(String canonicalUrl, Path workDir) {
        Optional<File> subtitleFile = ytDlpClient.downloadSubtitles(canonicalUrl, workDir);
        if (subtitleFile.isEmpty() || !subtitleFile.get().exists() || subtitleFile.get().length() == 0) {
            log.info("No existing subtitles found for {}", canonicalUrl);
            return Optional.empty();
        }

        List<TranscriptSegment> segments = parseVtt(subtitleFile.get());
        if (segments.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(segments);
    }

    /**
     * Parses standard WebVTT subtitles into transcript segments, removing HTML tags and deduplicating cues.
     */
    List<TranscriptSegment> parseVtt(File vttFile) {
        List<TranscriptSegment> segments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(vttFile))) {
            String line;
            double currentStart = 0;
            double currentEnd = 0;
            StringBuilder currentText = new StringBuilder();
            boolean inCue = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("WEBVTT") || line.startsWith("NOTE") || line.startsWith("STYLE")) {
                    if (inCue && !currentText.isEmpty()) {
                        String clean = cleanText(currentText.toString());
                        if (!clean.isBlank()) {
                            segments.add(new TranscriptSegment(currentStart, currentEnd, clean));
                        }
                        currentText.setLength(0);
                        inCue = false;
                    }
                    continue;
                }

                Matcher matcher = TIMESTAMP_PATTERN.matcher(line);
                if (matcher.find()) {
                    if (inCue && !currentText.isEmpty()) {
                        String clean = cleanText(currentText.toString());
                        if (!clean.isBlank()) {
                            segments.add(new TranscriptSegment(currentStart, currentEnd, clean));
                        }
                        currentText.setLength(0);
                    }
                    currentStart = parseTime(matcher.group().split(" --> ")[0]);
                    currentEnd = parseTime(matcher.group().split(" --> ")[1]);
                    inCue = true;
                } else if (inCue) {
                    if (!currentText.isEmpty()) {
                        currentText.append(" ");
                    }
                    currentText.append(line);
                }
            }

            if (inCue && !currentText.isEmpty()) {
                String clean = cleanText(currentText.toString());
                if (!clean.isBlank()) {
                    segments.add(new TranscriptSegment(currentStart, currentEnd, clean));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse VTT subtitle file {}: {}", vttFile.getName(), e.getMessage());
        }

        return deduplicateSegments(segments);
    }

    private double parseTime(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            if (parts.length == 3) {
                double hours = Double.parseDouble(parts[0]);
                double minutes = Double.parseDouble(parts[1]);
                double seconds = Double.parseDouble(parts[2]);
                return hours * 3600 + minutes * 60 + seconds;
            } else if (parts.length == 2) {
                double minutes = Double.parseDouble(parts[0]);
                double seconds = Double.parseDouble(parts[1]);
                return minutes * 60 + seconds;
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    private String cleanText(String raw) {
        // Strip out HTML tags e.g. <c> </c> <00:00:01.000>
        return raw.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
    }

    private List<TranscriptSegment> deduplicateSegments(List<TranscriptSegment> raw) {
        List<TranscriptSegment> result = new ArrayList<>();
        String previousText = "";
        for (TranscriptSegment seg : raw) {
            String text = seg.text().trim();
            if (!text.isEmpty() && !text.equalsIgnoreCase(previousText)) {
                result.add(seg);
                previousText = text;
            }
        }
        return result;
    }
}

