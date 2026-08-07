# AI Agent Context for fastjson2

High-performance Java JSON library (fastjson v2). Multi-module Maven project, JDK 8+.

## Key Modules

- `core/` — main library
- `kotlin/` — Kotlin extensions
- `extension/` — Arrow, ClickHouse, etc.
- `extension-spring5/`, `extension-spring6/`, `extension-solon/`, `extension-jaxrs/` — framework integrations
- `fastjson1-compatible/` — v1 compatibility
- `benchmark/` — JMH benchmarks

## Build & Test

```bash
mvn clean package        # build all
mvn test                 # run tests
mvn validate             # checkstyle + modernizer checks
```

## Conventions

- Java (primary), Kotlin (for `kotlin` module)
- Tests: JUnit 5 / Kotest
- Code style: Checkstyle (`src/checkstyle/fastjson2-checks.xml`), enforced via `mvn validate`
- Performance-first project: every change must consider performance impact
- Watch method bytecode size (codeSize) — keep hot-path methods under the two JIT inlining thresholds: 35 bytes (C1, `-XX:MaxInlineSize`) and 325 bytes (C2, `-XX:FreqInlineSize`) so they can be inlined
- Prefer the implementation with smaller codeSize when choosing between equivalent approaches
- PRs should be small and focused; ensure `mvn validate` and `mvn test` pass
- PRs and comments should be bilingual: English first, Chinese version collapsed in a `<details>` block
