# Architecture

> Sections marked [CURRENT] describe the repo as it is; [TARGET] describes the intended design. Do not treat [TARGET] as existing. Verify with the file tree.

## 1. [CURRENT] Layout

- `pom.xml` (Maven build configuration, Java 21, Spring Boot 3.3.4)
- `src/main/java/com/utkarsh/yt2text/`
  - `YouTubeToTextApplication.java` (Spring Boot entry point)
  - `config/`
    - `AppProperties.java` (Config properties for yt-dlp, Whisper, Translation)
    - `ClientConfig.java` (RestClient bean definition)
  - `controller/`
    - `ConversionController.java` (REST endpoints: `GET /`, `POST /api/convert`, `GET /{filename}`, `GET /api/health`)
  - `dto/`
    - `ConvertResponse.java` (API contract response)
    - `ErrorResponse.java` (Unified JSON error response matching frontend expectations)
    - `TranscriptSegment.java` (Timestamped segment representation)
  - `service/`
    - `UrlValidator.java` (Secure URL parsing, domain whitelisting, video ID extraction)
    - `CaptionService.java` (yt-dlp subtitle retrieval, VTT parsing, cue deduplication)
    - `TranscriptionService.java` (Speech-to-text fallback via Whisper HTTP client)
    - `TranslationService.java` (Swappable provider manager)
    - `ExportService.java` (Plain text, SRT, WebVTT, JSON formatters)
    - `ConversionPipelineService.java` (Pipeline orchestrator with safe memory cache & cleanup)
  - `client/`
    - `YtDlpClient.java` & `ProcessYtDlpClient.java` (ProcessBuilder execution with timeouts and argument lists)
    - `WhisperClient.java` & `HttpWhisperClient.java` (HTTP client for OpenAI/Whisper compatible APIs)
    - `TranslationProvider.java` & `MyMemoryTranslationProvider.java` (Translation provider interface & HTTP client)
  - `exception/`
    - `Yt2TextException.java` (Domain root exception)
    - `InvalidUrlException.java`, `CaptionNotFoundException.java`, `TranscriptionException.java`, `TranslationException.java`, `ResourceNotFoundException.java`
    - `GlobalExceptionHandler.java` (@RestControllerAdvice mapping exceptions to JSON)
- `src/main/resources/`
  - `application.properties`
  - `static/index.html`, `static/space.txt` (Frontend Web UI & assets)
- `src/test/java/com/utkarsh/yt2text/`
  - `controller/ConversionControllerTest.java` (MockMvc tests for health and convert endpoints)
  - `service/UrlValidatorTest.java` (Comprehensive URL validation & injection protection tests)
  - `service/CaptionServiceTest.java` (VTT parsing & subtitle retrieval unit tests)
  - `service/TranscriptionServiceTest.java` (Audio fallback unit tests)
  - `service/ConversionPipelineServiceTest.java` (Branching logic tests)
  - `service/ExportServiceTest.java` (txt, srt, vtt, json exporter tests)
- `.github/workflows/ci.yml` (GitHub Actions CI for Java 21, yt-dlp, ffmpeg, `mvn verify`)
- `Dockerfile` & `docker-compose.yml` (Multi-stage build containerization)
- `.env.example`, `.gitignore`

## 2. Data Flow

1. User accesses `GET /` and enters a YouTube URL on the web UI.
2. Web UI submits `POST /api/convert` with `youtube_url` parameter.
3. `UrlValidator` verifies YouTube domain whitelisting and extracts 11-char video ID, converting it to canonical URL.
4. `ConversionPipelineService` runs:
   - **Step A (Captions)**: Calls `CaptionService` -> `ProcessYtDlpClient` with timeout to extract available subtitles (`.vtt`).
   - **Step B (Fallback STT)**: If no captions exist, calls `TranscriptionService` -> downloads audio and invokes Whisper API.
   - **Step C (Translation)**: Translates English transcript into Hindi and Marathi via `TranslationService`.
   - **Step D (Export & Cache)**: Generates output files into a memory cache identified by safe UUID tokens to prevent directory traversal attacks.
5. All temp audio or caption files on disk are cleaned up inside a `finally` block.
6. The user downloads transcripts via `GET /{filename}`.

## 3. Security Decisions

- **No Shell Concatenation**: Subprocesses use `ProcessBuilder` with immutable argument lists.
- **Path Traversal Elimination**: Replaced arbitrary file reading with an in-memory download token map.
- **Clean Teardown**: Guaranteed deletion of temporary directories on completion or failure.
