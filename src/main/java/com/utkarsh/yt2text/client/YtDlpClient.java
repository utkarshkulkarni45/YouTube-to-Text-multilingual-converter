package com.utkarsh.yt2text.client;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface YtDlpClient {
    /**
     * Checks if subtitles/captions exist and downloads them as VTT.
     *
     * @param canonicalUrl validated YouTube URL
     * @param workDir directory for temporary file
     * @return downloaded caption file if available
     */
    Optional<File> downloadSubtitles(String canonicalUrl, Path workDir);

    /**
     * Downloads audio as MP3/m4a for fallback speech-to-text.
     *
     * @param canonicalUrl validated YouTube URL
     * @param workDir directory for temporary file
     * @return downloaded audio file
     */
    File downloadAudio(String canonicalUrl, Path workDir);
}

