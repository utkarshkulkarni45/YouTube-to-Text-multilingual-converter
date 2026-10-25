package com.utkarsh.yt2text.exception;

public class TranslationException extends Yt2TextException {
    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}

