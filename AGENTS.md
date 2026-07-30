# AI Agent Context for fastjson2

High-performance Java JSON library (fastjson v2) by Alibaba. Multi-module Maven project targeting JDK 8+.

## Key Modules

| Module | Artifact | Notes |
|--------|----------|-------|
| `core/` | `fastjson2` | Main library. Entrypoint: `com.alibaba.fastjson2.JSON` |
| `kotlin/` | `fastjson2-kotlin` | Kotlin extensions, sources in `src/main/kotlin` |
| `extension/` | `fastjson2-extension` | Arrow, ClickHouse, Geo, Retrofit, etc. |
| `extension-spring5/` | `fastjson2-extension-spring5` | Spring 5.x MVC / WebFlux / Data Redis / Messaging |
| `extension-spring6/` | `fastjson2-extension-spring6` | Spring 6.x — **only builds on JDK 17+** |
| `extension-solon/` | `fastjson2-extension-solon` | Solon framework integration |
| `extension-jaxrs/` | `fastjson2-extension-jaxrs` | JAX-RS (contains `extension-jaxrs-javax/` and `extension-jaxrs-jakarta/`) |
| `fastjson1-compatible/` | `fastjson` (groupId `com.alibaba`) | v1 drop-in compat layer |
| `codegen/` + `codegen-test/` | — | APT code generation — **only builds on JDK ≤22** |
| `benchmark/` | — | JMH benchmarks (JDK 8+) |
| `benchmark_25/` | — | Additional benchmarks — **only builds on JDK 25+** |
| `safemode-test/` | — | SafeMode integration test suite |
| `test-jdk17/` | — | JDK 17 feature tests (Records, sealed classes) |
| `test-jdk25/` | — | JDK 25 feature tests |

> **Module activation depends on your JDK.** `extension-spring6`, `example-spring6-test` need JDK 17+. `codegen`/`codegen-test` need JDK ≤22. `benchmark_25` and `test-jdk25` need JDK 25+. The CI matrix runs JDK 8/11/17/21/25 × ubuntu/windows/macos.

## Build & Test

Always use the Maven wrapper:

```bash
./mvnw clean package                         # build all modules
./mvnw -pl core clean package                # build a single module (fast iteration)
./mvnw -pl core -Dtest=JSONTest test         # run a single test class
./mvnw validate                              # checkstyle + modernizer + POM enforcer
./mvnw -V --no-transfer-progress clean package  # CI-style verbose build
```

### Test variants

```bash
./mvnw clean test                              # normal (ASM-accelerated readers/writers)
./mvnw -Dfastjson2.creator=reflect clean test  # reflect mode (no ASM bytecode — validates code paths)
```

The `fastjson2.creator` system property switches between ASM-backed (`ObjectReaderCreatorASM`) and reflection-based (`ObjectReaderCreator`) reader/writer factories. Some test classes are `@DisabledIfSystemProperty` when in reflect mode.

The CI runs **both** modes on every JDK/OS combination.

### Module-specific Surefire config

The Kotlin module's surefire config includes `*.kt` files explicitly. The core module sets `user.timezone=Asia/Shanghai` — some tests are timezone-sensitive. Default CI value: `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai`.

## Architecture

```
com.alibaba.fastjson2
├── JSON.java              # Main API (parseObject, toJSONString, etc.)
├── JSONB.java             # Binary JSON format
├── JSONPath.java          # SQL:2016-compatible JSONPath engine
├── JSONReader*.java       # Encoding-specific parsers (UTF8/UTF16/ASCII/JSONB)
├── JSONWriter*.java       # Encoding-specific writers (UTF8/UTF16/JSONB)
├── reader/                # Object deserialization (ObjectReader, FieldReader, ObjectReaderCreator/ASM)
├── writer/                # Object serialization (ObjectWriter, FieldWriter, ObjectWriterCreator/ASM)
├── annotation/            # @JSONField, @JSONType, @JSONCreator, @JSONCompiler
├── filter/                # NameFilter, ValueFilter, PropertyFilter, etc.
├── schema/                # JSON Schema validation
├── support/               # CSV, GeoJSON, Retrofit, etc.
└── util/                  # Internal utilities (BeanUtils, TypeUtils, FieldInfo)
```

## Conventions

- **Java 8 baseline** — `maven.compiler.source/target=8`. No `var`, no streams-only APIs above Java 8.
- **No wildcard imports** — organized alphabetically.
- **Checkstyle** (`src/checkstyle/fastjson2-checks.xml`) + **modernizer** (`src/violations.xml` — bans `Class.newInstance()`, `String.getBytes()` without charset, etc.) + **pedantic POM enforcer** (section order, dependency ordering).
- All features (`JSONWriter.Feature`, `JSONReader.Feature`) are **OFF by default** (unlike fastjson 1.x).
- Tests: **JUnit 5** (core) or **Kotest** (kotlin module). Lombok is test-scope only.
- The `kotlin-maven-plugin` must appear **above** `maven-compiler-plugin` in POM plugin order.
- Module-info descriptors are generated at `package` phase via `moditect-maven-plugin`, from `src/main/moditect/module-info.java`.

## Useful details

- **Package**: `com.alibaba.fastjson2` (not `com.alibaba.fastjson` — that's the compat layer).
- **Coverage**: JaCoCo activates when `CI=true` env var is set. JaCoCo excludes benchmarks, examples, and test sources.
- **Version bump**: `scripts/bump_fastjson2_version <version>` uses `versions-maven-plugin` to update all POMs.
- **Release**: activated via `-DperformRelease=true`, which enables source jars, javadoc, GPG signing, Dokka (Kotlin), and git-properties embedding.
- **CI**: 20-minute timeout, matrix 5 JDKs × 3 OSes, `fail-fast: false`. Skips on `docs/**` and `**.md` changes.
