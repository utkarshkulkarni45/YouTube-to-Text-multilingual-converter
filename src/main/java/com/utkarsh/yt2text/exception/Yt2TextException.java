package com.utkarsh.yt2text.exception;

public class Yt2TextException extends RuntimeException {
    public Yt2TextException(String message) {
        super(message);
    }

    public Yt2TextException(String message, Throwable cause) {
        super(message, cause);
    }
}

