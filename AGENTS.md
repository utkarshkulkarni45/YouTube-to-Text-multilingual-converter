# AGENTS.md

This repository uses a shared AI-governance system. Any AI agent (Claude, Cursor, ChatGPT, Copilot) MUST:

1. Read, in this order: `ai/rules.md` → `ai/problem.md` → `ai/architecture.md` → `ai/requirements.md` → `ai/progress.md` → `ai/context.md`.
2. Work only on the git branch `upgrade`. Never commit to `main`.
3. Never assume a file, class, endpoint, or library exists. Verify in the repo first.
4. Run the build/tests and report real output before claiming anything is done.
5. At the end of every session, update `ai/progress.md` (handoff section).

Stack: Java 21, Spring Boot 3, Maven, JUnit 5, Docker.
