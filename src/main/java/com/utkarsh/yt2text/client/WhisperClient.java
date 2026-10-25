package com.utkarsh.yt2text.client;

import java.io.File;

public interface WhisperClient {
    /**
     * Transcribes an audio file into English text using a Whisper API.
     *
     * @param audioFile audio file to transcribe
     * @return transcribed text
     */
    String transcribe(File audioFile);
}

