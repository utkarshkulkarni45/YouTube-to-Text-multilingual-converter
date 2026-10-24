# Problem & Vision

## Confirmed Facts

- Project: YouTube-to-Text-multilingual-converter (GitHub: utkarshkulkarni45).
- Owner goal: make this repo a strong portfolio piece that attracts recruiters.

## Problem

Valuable information is locked inside videos. People who want to read, search, quote, translate, or study a video's content have to watch it in full, and non-native speakers face an extra language barrier. YouTube's built-in transcripts are hard to export, are often missing or inaccurate, and are not conveniently available in the reader's language.

## Solution

A tool that takes a YouTube link and returns clean text in the language the user needs, using existing captions when available and speech recognition when not.

## Target Users

- Students and researchers extracting lecture content.
- Non-native speakers reading content in their own language.
- Content creators repurposing videos into articles/notes.
- Accessibility-minded users who prefer text.

## Success Criteria

- A new visitor understands the project in under 30 seconds from the README.
- A recruiter can try it via a live demo link without installing anything.
- `docker compose up` or one pip command runs it locally.
- CI badge is green; tests cover the core pipeline.
- Code demonstrates: clean architecture, error handling, testing, CI/CD, containerization, and API/UI design.

## Recruiter-Facing Positioning (use in README)

"Multilingual YouTube transcription tool: caption-first pipeline with speech-to-text fallback, translation, multiple export formats, tests, CI, and Docker."
Add measurable claims only after measuring them (e.g., "transcribes a 10-min video in X s on CPU"). Do not state unmeasured numbers.

## Non-Goals

Not a video downloader. Not a replacement for professional transcription. No bypassing access controls.
