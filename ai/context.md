# Context (quick start for any agent)

## One-liner

Web app + REST API that converts a YouTube link into text (and subtitle files) in the user's chosen language. Being rebuilt from Python to Java 21 + Spring Boot 3 on branch `upgrade`.

## Owner

GitHub user utkarshkulkarni45. Local path (Windows): D:\Java Projects\YouTube-to-Text-multilingual-converter-main. Primary goal: a portfolio project that attracts recruiters.

## Read order

AGENTS.md → ai/rules.md → ai/problem.md → ai/architecture.md → ai/requirements.md → ai/progress.md.

## Current state

See ai/progress.md "Status Snapshot". Legacy Python files remain until the Java version works and the owner approves removal.

## Common commands (after the Maven project exists)

- Build + test: ./mvnw verify (Windows: mvnw.cmd verify)
- Run: ./mvnw spring-boot:run
- Docker: docker compose up --build
- Branch check: git branch --show-current (must print: upgrade)

## Environment variables (names only; values live in .env, never in git)

[VERIFY: finalize during implementation] e.g. STT_API_KEY, TRANSLATION_API_KEY, PORT.

## Glossary

- Captions: subtitles YouTube already provides (manual or auto-generated).
- STT: speech-to-text fallback used when no captions exist.
- Pipeline: validate → captions → (STT) → translate → export.
