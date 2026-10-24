package com.utkarsh.yt2text.service;

import com.utkarsh.yt2text.exception.InvalidUrlException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates YouTube URLs and extracts the 11-character video ID.
 * Supports standard watch URLs, short URLs (youtu.be), and shorts.
 */
@Service
public class UrlValidator {

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com",
            "youtu.be"
    );

    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{11}$");
    private static final Pattern SHORTS_PATTERN = Pattern.compile("^/shorts/([a-zA-Z0-9_-]{11})");

    /**
     * Extracts and validates the video ID from a YouTube URL.
     *
     * @param rawUrl user supplied URL
     * @return 11-character YouTube video ID
     * @throws InvalidUrlException if URL is null, malformed, non-YouTube, or lacks valid video ID
     */
    public String extractVideoId(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new InvalidUrlException("Invalid YouTube URL format.");
        }

        String trimmed = rawUrl.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }

        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException e) {
            throw new InvalidUrlException("Invalid YouTube URL format.");
        }

        String host = uri.getHost();
        if (host == null) {
            throw new InvalidUrlException("Invalid YouTube URL format.");
        }
        host = host.toLowerCase();
        if (!ALLOWED_HOSTS.contains(host)) {
            throw new InvalidUrlException("Invalid YouTube URL format.");
        }

        String videoId = null;
        if (host.equals("youtu.be")) {
            String path = uri.getPath();
            if (path != null && path.length() > 1) {
                videoId = path.substring(1);
            }
        } else {
            String path = uri.getPath();
            if (path != null) {
                Matcher shortsMatcher = SHORTS_PATTERN.matcher(path);
                if (shortsMatcher.find()) {
                    videoId = shortsMatcher.group(1);
                }
            }

            if (videoId == null && uri.getQuery() != null) {
                String[] params = uri.getQuery().split("&");
                for (String param : params) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2 && "v".equalsIgnoreCase(kv[0])) {
                        videoId = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        break;
                    }
                }
            }
        }

        if (videoId == null || !VIDEO_ID_PATTERN.matcher(videoId).matches()) {
            throw new InvalidUrlException("Invalid YouTube URL format.");
        }

        return videoId;
    }

    /**
     * Canonicalizes the YouTube URL to avoid shell/parameter injection.
     */
    public String buildCanonicalUrl(String videoId) {
        if (videoId == null || !VIDEO_ID_PATTERN.matcher(videoId).matches()) {
            throw new InvalidUrlException("Invalid YouTube video ID.");
        }
        return "https://www.youtube.com/watch?v=" + videoId;
    }
}

