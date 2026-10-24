# Functional Requirements & Verification Matrix

Status legend: `[x]` verified in code and tests · `[ ]` not done

## A. Core Conversion (P0)

- [x] FR-01 Accept a YouTube URL (watch, youtu.be, shorts formats) and validate domain/format. Tested in `UrlValidatorTest`.
- [x] FR-02 Produce a text transcript of the video's spoken content. Tested in `ConversionPipelineServiceTest`.
- [x] FR-03 Support multiple input languages (auto-detect captions via yt-dlp `--sub-lang "en.*,en"`).
- [x] FR-04 Output in multilingual conversion (English, Hindi, Marathi). Tested in `ConversionPipelineServiceTest`.
- [x] FR-05 Prefer existing YouTube captions; fall back to speech-to-text (Whisper) when captions are unavailable. Tested in `ConversionPipelineServiceTest`.
- [x] FR-06 Clear, specific error messages: invalid URL, no captions, provider failure. Handled via `GlobalExceptionHandler` and tested in `ConversionControllerTest`.

## B. Output & Serving (P0/P1)

- [x] FR-07 Display transcript download links in UI. Verified with `index.html` static serving.
- [x] FR-08 Export as `.txt`, `.srt`, `.vtt`, `.json`. Tested in `ExportServiceTest`.
- [x] FR-09 Secure download endpoint (`GET /{filename}`) without directory traversal vulnerability. Tested in `ConversionControllerTest`.
- [x] FR-10 Health check endpoint (`GET /api/health`). Tested in `ConversionControllerTest`.

## C. Quality & Engineering (P0 for Recruiter-Readiness)

- [x] NFR-01 29 JUnit 5 unit and MockMvc integration tests covering all pipeline branches and edge cases (`BUILD SUCCESS`).
- [x] NFR-02 CI via GitHub Actions (`.github/workflows/ci.yml`): Java 21, yt-dlp, ffmpeg, `mvn verify`.
- [x] NFR-03 Multi-stage Dockerfile and `docker-compose.yml` with JRE 21, yt-dlp, ffmpeg.
- [x] NFR-04 Config via environment variables and `application.properties`; `.env.example` provided.
- [x] NFR-05 Safe temporary file handling (recursive directory cleanup in `finally` blocks).
