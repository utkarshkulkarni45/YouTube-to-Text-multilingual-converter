package com.utkarsh.yt2text.exception;

import com.utkarsh.yt2text.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrl(InvalidUrlException ex) {
        log.warn("Invalid URL requested: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.ofDetail(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.ofDetail(ex.getMessage()));
    }

    @ExceptionHandler(CaptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCaptionNotFound(CaptionNotFoundException ex) {
        log.warn("Caption not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.ofError(ex.getMessage()));
    }

    @ExceptionHandler(TranscriptionException.class)
    public ResponseEntity<ErrorResponse> handleTranscription(TranscriptionException ex) {
        log.error("Transcription error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.ofDetail("Error during transcription: " + ex.getMessage()));
    }

    @ExceptionHandler(TranslationException.class)
    public ResponseEntity<ErrorResponse> handleTranslation(TranslationException ex) {
        log.error("Translation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.ofDetail("Error during translation: " + ex.getMessage()));
    }

    @ExceptionHandler(Yt2TextException.class)
    public ResponseEntity<ErrorResponse> handleGenericYt2Text(Yt2TextException ex) {
        log.error("Internal service error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.ofDetail(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex) {
        log.error("Unhandled server exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.ofDetail("Internal server error: " + ex.getMessage()));
    }
}

