package com.utkarsh.yt2text.exception;

public class TranscriptionException extends Yt2TextException {
    public TranscriptionException(String message) {
        super(message);
    }

    public TranscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

