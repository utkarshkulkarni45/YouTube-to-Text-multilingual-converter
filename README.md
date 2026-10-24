# YouTube-to-Text Multilingual Converter

[![CI](https://github.com/utkarshkulkarni45/YouTube-to-Text-multilingual-converter/actions/workflows/ci.yml/badge.svg)](https://github.com/utkarshkulkarni45/YouTube-to-Text-multilingual-converter/actions/workflows/ci.yml)

A Spring Boot 3 service that turns a YouTube link into clean text in English, Hindi, and Marathi. Caption-first pipeline with speech-to-text fallback, translation, and multiple export formats (txt, srt, vtt, json). Tests, CI, and Docker included.

**Live demo:** _[placeholder - to be added]_

## Features

- Accepts `watch`, `youtu.be`, and `shorts` YouTube URLs with strict host allow-listing.
- Prefers existing YouTube captions (manual or auto-generated); falls back to Whisper speech-to-text when none exist.
- Translates transcripts into Hindi and Marathi.
- Exports as `.txt`, `.srt`, `.vtt`, and `.json`.
- Secure download tokens (no path traversal), temp files cleaned in `finally` blocks.
- Consistent JSON error responses via a global exception handler.
- `GET /api/health` for readiness checks.

## Architecture

```mermaid
flowchart LR
  U[User / Browser] -->|GET /| C[ConversionController]
  U -->|POST /api/convert| C
  C --> P[ConversionPipelineService]
  P --> V[UrlValidator]
  P --> CS[CaptionService]
  CS --> Y[YtDlpClient (ProcessBuilder)]
  P -->|no captions| TS[TranscriptionService]
  TS --> W[WhisperClient (HTTP)]
  P --> TR[TranslationService]
  TR --> TP[TranslationProvider interface]
  P --> E[ExportService]
  E --> Cache[(In-memory token cache)]
  C -->|GET /{token}| Cache
```

## Tech stack

Java 21, Spring Boot 3.3, Maven, JUnit 5, Mockito, Spring MockMvc, Docker (multi-stage), GitHub Actions.

## Run locally

Prerequisites: JDK 21, Maven (or `mvnw`), `yt-dlp`, `ffmpeg` on PATH.

```bash
cp .env.example .env
# edit .env as needed
mvn spring-boot:run
```

Open http://localhost:8000.

## Run with Docker

```bash
docker compose up --build
```

## API examples

Health:

```bash
curl http://localhost:8000/api/health
```

Convert:

```bash
curl -X POST http://localhost:8000/api/convert -F "youtube_url=https://www.youtube.com/watch?v=dQw4w9WgXcQ"
```

Download (token from convert response):

```bash
curl -O http://localhost:8000/<token>
```

## Environment variables

See `.env.example`: `PORT`, `YT_DLP_PATH`, `OPENAI_API_KEY`, `TRANSLATION_PROVIDER`.

## Limitations

- Requires `yt-dlp` and `ffmpeg` installed (or provided via Docker).
- Speech-to-text fallback requires an OpenAI/Whisper-compatible API key; without it, videos without captions return an informative error.
- Translation uses a free provider (MyMemory) with rate limits.
- No persistent storage; results are held in an in-memory cache.

## Roadmap

- Add rate limiting and request size limits.
- Persist conversion history.
- Support additional target languages.
- Deploy a live demo.

## License

MIT (placeholder).