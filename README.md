# YouTube-to-Text Multilingual Converter

[![CI](https://github.com/utkarshkulkarni45/YouTube-to-Text-multilingual-converter/actions/workflows/ci.yml/badge.svg)](https://github.com/utkarshkulkarni45/YouTube-to-Text-multilingual-converter/actions/workflows/ci.yml)

A **Spring Boot 3** service that converts a YouTube video into clean text in **English**, **Hindi**, and **Marathi**. The pipeline prefers existing YouTube captions and falls back to Whisper‑compatible speech‑to‑text when captions are missing. The result can be exported as **txt**, **srt**, **vtt**, or **json**.

---

## 🎬 Live demo
**_Placeholder – to be added_**

---

## ✨ Features
- Accepts YouTube URLs from `watch`, `youtu.be`, and `shorts` domains (strict host whitelist).
- Caption‑first extraction using `yt‑dlp`; automatic fallback to Whisper transcription when captions are unavailable.
- Translation of the transcript into Hindi and Marathi using a pluggable translation provider (default: MyMemory).
- Export formats: `.txt`, `.srt`, `.vtt`, `.json`.
- Secure download tokens – no path traversal, temporary files are cleaned up.
- Consistent JSON error responses via a global `@RestControllerAdvice`.
- Health endpoint (`GET /api/health`).
- Full test suite (JUnit 5 + Mockito) and CI pipeline.
- Docker‑ready multi‑stage image.

---

## 🏗️ Architecture
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
    E --> Cache[(In‑memory token cache)]
    C -->|GET /{token}| Cache
```

---

## 🛠️ Tech Stack
| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Build | Maven |
| Testing | JUnit 5, Mockito, Spring MockMvc |
| Containerisation | Docker (multi‑stage) |
| CI/CD | GitHub Actions |
| External tools | `yt‑dlp`, `ffmpeg` |
| STT provider | Whisper‑compatible API (OpenAI) |
| Translation provider | MyMemory (free) |

---

## 🚀 Run locally
**Prerequisites**
- JDK 21 (or newer)
- Maven
- `yt‑dlp` and `ffmpeg` available on your `PATH`

```bash
# Clone the repo and navigate to the project root
git clone https://github.com/utkarshkulkarni45/YouTube-to-Text-multilingual-converter.git
cd YouTube-to-Text-multilingual-converter

# Create a local .env file from the example
cp .env.example .env   # edit values as needed (e.g. OPENAI_API_KEY)

# Build and run the Spring Boot app
mvn spring-boot:run
```
The UI will be available at `http://localhost:8000`.

---

## 🐳 Run with Docker
```bash
# Build the image (multi‑stage Dockerfile)
docker compose build

# Start the container
docker compose up -d
```
The service will be reachable at `http://localhost:8000` inside the container.

---

## 🌐 API examples
```bash
# Health check
curl http://localhost:8000/api/health

# Convert a video (replace <URL> with a real YouTube link)
curl -X POST http://localhost:8000/api/convert \
     -F "youtube_url=<URL>"

# Download a generated file (token comes from the JSON response of the convert call)
curl -O http://localhost:8000/<token>
```
All responses are JSON unless a file download is requested.

---

## ⚙️ Environment variables
| Variable | Description |
|----------|-------------|
| `SERVER_PORT` | Port on which the app runs (default 8000) |
| `YT_DLP_PATH` | Absolute path to the `yt‑dlp` binary (default assumes it is on `PATH`) |
| `OPENAI_API_KEY` | API key for the Whisper STT service |
| `APP_TRANSLATION_PROVIDER` | Identifier of the translation provider (`mymemory` by default) |

See the **`.env.example`** file for a template.

---

## 🚧 Limitations
- Requires internet connectivity and the external `yt‑dlp`/`ffmpeg` tools.
- Whisper transcription works only when a valid `OPENAI_API_KEY` is supplied.
- The free MyMemory translation API imposes rate limits and a maximum request size (~500 chars).
- No persistent storage – results are kept only in memory for the duration of the request.

---

## 📈 Roadmap (placeholder)
- Rate‑limiting & request‑size validation.
- Persistent conversion history.
- Additional target languages.
- Deploy a public demo instance.

---

## 📄 License
MIT (placeholder).
