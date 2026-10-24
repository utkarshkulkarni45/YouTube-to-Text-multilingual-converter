# Progress Log

> Honesty rule: only list work as Done if it is visible in the repo or commit history. Update at the end of every session.

## Status Snapshot

- Last updated: 2026-10-24
- Last agent/session: Senior Java Backend Engineer
- Branch: `upgrade`
- Overall state: Java 21 + Spring Boot 3 migration completed, 29/29 tests passing (`mvn verify` green).

## Done This Session

1. **Architecture & Project Setup**:
   - Initialized Maven Spring Boot 3.3.4 project targeting Java 21 (`pom.xml`).
   - Configured layered structure: `controller`, `service`, `client`, `dto`, `exception`, `config`.
   - Moved and mounted `index.html` and `static/` to `src/main/resources/static/`.
   - Added `.env.example` and enhanced `.gitignore` for Java/Maven/Spring Boot and environment secrets.

2. **Core Domain Services & Clients**:
   - `UrlValidator`: Enforces YouTube host whitelisting (`youtube.com`, `youtu.be`, `m.youtube.com`), extracts 11-char video IDs, canonicalizes URLs to protect against command injection.
   - `YtDlpClient` / `ProcessYtDlpClient`: Subprocess execution using `ProcessBuilder` with argument lists (no shell concatenation), timeouts, and safe stream consumption.
   - `CaptionService`: Fetches official/auto-generated captions via `yt-dlp`, parses WebVTT, removes HTML/styling artifacts, and deduplicates cues.
   - `WhisperClient` / `HttpWhisperClient` & `TranscriptionService`: Fallback STT pipeline invoked only when no captions exist, using Spring `RestClient` to call Whisper-compatible endpoints.
   - `TranslationProvider` / `MyMemoryTranslationProvider` & `TranslationService`: Pluggable translation interface supporting language translation (English -> Hindi & Marathi) with graceful degradation.
   - `ExportService`: Formats transcripts into `.txt`, SubRip `.srt`, WebVTT `.vtt`, and `.json`.
   - `ConversionPipelineService`: Orchestrates URL validation -> caption extraction -> fallback audio transcription -> translation -> format caching. Cleans up temp disk directories in `finally` blocks.
   - Memory-safe download token cache in `ConversionPipelineService` eliminating legacy arbitrary file path traversal vulnerability.

3. **HTTP Controller & Error Handling**:
   - `ConversionController`: Implemented `GET /api/health`, `POST /api/convert` (supporting `multipart/form-data` and `application/x-www-form-urlencoded`), and `GET /{filename}` preserving legacy UI frontend contract.
   - `GlobalExceptionHandler`: Unified `@RestControllerAdvice` mapping domain exceptions (`InvalidUrlException`, `CaptionNotFoundException`, `TranscriptionException`, etc.) to structured JSON errors.

4. **Testing & Quality Assurance**:
   - Implemented 29 JUnit 5 unit and `MockMvc` integration tests:
     - `UrlValidatorTest`: URL formats (watch, youtu.be, shorts, queries) & malicious/invalid host rejection.
     - `CaptionServiceTest`: VTT parser and missing caption handling with mocked `YtDlpClient`.
     - `TranscriptionServiceTest`: Audio download and Whisper API client integration testing.
     - `ConversionPipelineServiceTest`: Branch verification (caption path vs fallback transcription path).
     - `ExportServiceTest`: Format validation for TXT, SRT, VTT, and JSON.
     - `ConversionControllerTest`: `MockMvc` testing for `/api/health`, `/api/convert`, download file route, and error handler mappings.
   - Configured `mock-maker-subclass` to ensure robust Mockito bytecode generation on Java 21 / Java 25.
   - Ran `mvn clean compile` and `mvn verify`: **29 tests run, 0 failures, 0 errors, BUILD SUCCESS**.

5. **CI/CD & Containerization**:
   - Added GitHub Actions workflow (`.github/workflows/ci.yml`): Sets up JDK 21, installs `ffmpeg` & `yt-dlp`, executes `mvn -B verify` on push/PR.
   - Multi-stage `Dockerfile`: Stage 1 builds fat JAR with Temurin 21; Stage 2 creates minimal Alpine runtime with `ffmpeg`, `yt-dlp`, and JRE 21.
   - Added `docker-compose.yml`.

## Files Changed / Added

- `pom.xml`
- `.gitignore`
- `.env.example`
- `Dockerfile`
- `docker-compose.yml`
- `.github/workflows/ci.yml`
- `src/main/resources/application.properties`
- `src/main/resources/static/index.html`
- `src/main/resources/static/space.txt`
- `src/main/java/com/utkarsh/yt2text/YouTubeToTextApplication.java`
- `src/main/java/com/utkarsh/yt2text/config/AppProperties.java`
- `src/main/java/com/utkarsh/yt2text/config/ClientConfig.java`
- `src/main/java/com/utkarsh/yt2text/controller/ConversionController.java`
- `src/main/java/com/utkarsh/yt2text/dto/ConvertResponse.java`
- `src/main/java/com/utkarsh/yt2text/dto/ErrorResponse.java`
- `src/main/java/com/utkarsh/yt2text/dto/TranscriptSegment.java`
- `src/main/java/com/utkarsh/yt2text/exception/Yt2TextException.java`
- `src/main/java/com/utkarsh/yt2text/exception/InvalidUrlException.java`
- `src/main/java/com/utkarsh/yt2text/exception/CaptionNotFoundException.java`
- `src/main/java/com/utkarsh/yt2text/exception/TranscriptionException.java`
- `src/main/java/com/utkarsh/yt2text/exception/TranslationException.java`
- `src/main/java/com/utkarsh/yt2text/exception/ResourceNotFoundException.java`
- `src/main/java/com/utkarsh/yt2text/exception/GlobalExceptionHandler.java`
- `src/main/java/com/utkarsh/yt2text/client/YtDlpClient.java`
- `src/main/java/com/utkarsh/yt2text/client/ProcessYtDlpClient.java`
- `src/main/java/com/utkarsh/yt2text/client/WhisperClient.java`
- `src/main/java/com/utkarsh/yt2text/client/HttpWhisperClient.java`
- `src/main/java/com/utkarsh/yt2text/client/TranslationProvider.java`
- `src/main/java/com/utkarsh/yt2text/client/MyMemoryTranslationProvider.java`
- `src/main/java/com/utkarsh/yt2text/service/UrlValidator.java`
- `src/main/java/com/utkarsh/yt2text/service/CaptionService.java`
- `src/main/java/com/utkarsh/yt2text/service/TranscriptionService.java`
- `src/main/java/com/utkarsh/yt2text/service/TranslationService.java`
- `src/main/java/com/utkarsh/yt2text/service/ExportService.java`
- `src/main/java/com/utkarsh/yt2text/service/ConversionPipelineService.java`
- `src/test/java/com/utkarsh/yt2text/controller/ConversionControllerTest.java`
- `src/test/java/com/utkarsh/yt2text/service/UrlValidatorTest.java`
- `src/test/java/com/utkarsh/yt2text/service/CaptionServiceTest.java`
- `src/test/java/com/utkarsh/yt2text/service/TranscriptionServiceTest.java`
- `src/test/java/com/utkarsh/yt2text/service/ConversionPipelineServiceTest.java`
- `src/test/java/com/utkarsh/yt2text/service/ExportServiceTest.java`
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
- `ai/architecture.md`
- `ai/requirements.md`
- `ai/progress.md`

## Commands Run & Real Results

- `git branch --show-current`: `upgrade`
- `mvn compile`: `BUILD SUCCESS` (Compiled 26 source files with javac release 21)
- `mvn test`: `BUILD SUCCESS` (29 tests run, 0 failures, 0 errors)
- `mvn verify`: `BUILD SUCCESS` (Packaged `target/youtube-to-text-1.0.0.jar` with repackaged Spring Boot archive)

## Open Questions / [VERIFY]

- Local Docker daemon is not installed or not in PATH on the host Windows machine (`The term 'docker' is not recognized`). The Dockerfile and docker-compose configurations are ready and conform to multi-stage temurin:21 specifications.
- Removal of legacy Python files (`main.py`, `requirements.txt`, `__pycache__`) is pending owner confirmation per instructions.

## Exact Next Step

1. Complete Step 4 documentation: rewrite `README.md` to be recruiter-ready with architecture diagram (mermaid), features, usage, curl examples, Docker instructions, and CI badge.
2. Obtain confirmation to remove legacy Python files.
3. Review git changes and prepare commits on branch `upgrade`.

## Handoff Update (2026-10-25)
- Rewrote README.md (recruiter-ready: mermaid arch, tech stack, run/Docker, curl examples, env vars, limitations, roadmap, CI badge, demo placeholder).
- mvn verify: BUILD SUCCESS (29 tests).
- Legacy Python files (main.py, requirements.txt, __pycache__) still present pending owner confirmation.
- Next: confirm deletion, then commit changes on branch upgrade.
