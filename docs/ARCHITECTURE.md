# Fastjson2 Architecture

## Overview

Fastjson2 is a high-performance JSON library for Java, targeting JDK 8 and later. This document describes the trimmed codebase, which consists of a single `core` module that handles text JSON serialization and deserialization. The library is organized into three layers: the public API, encoding-specific readers and writers, and the object mapping layer that produces `ObjectReader` and `ObjectWriter` instances for Java types.

## Project Structure

```
fastjson2/
├── core/                      # The library module (JDK 8+); the only Maven module
├── docs/                      # Documentation (English and Chinese)
├── src/                       # Build configuration (checkstyle rules, modernizer violations)
├── scripts/                   # Helper scripts (version bump)
├── .github/workflows/         # CI workflow (ci.yaml)
├── pom.xml, mvnw, mvnw.cmd    # Maven build and wrapper
└── README.md, README_cn.md    # Project readmes
```

## Core Module Architecture

### Component Overview

```
┌────────────────────────────────────────────────────────────────────┐
│                          JSON API Layer                            │
│   JSON · JSONObject · JSONArray · JSONFactory · TypeReference      │
│   JSONReader · JSONWriter · SymbolTable · PropertyNamingStrategy   │
├────────────────────────────────────────────────────────────────────┤
│   ┌──────────────────────┐      ┌──────────────────────┐           │
│   │      Reader Layer     │      │      Writer Layer     │           │
│   │   JSONReaderUTF8     │      │   JSONWriterUTF8     │           │
│   │   JSONReaderUTF16    │      │   JSONWriterUTF16    │           │
│   │   JSONReaderASCII    │      │   (JDK 8/9 variants) │           │
│   └─────────┬────────────┘      └─────────┬────────────┘           │
│   ┌─────────┴────────────┐      ┌─────────┴────────────┐           │
│   │  Object Mapping (r)   │      │  Object Mapping (w)   │           │
│   │   ObjectReader        │      │   ObjectWriter        │           │
│   │   ObjectReaderCreator │      │   ObjectWriterCreator │           │
│   │   ObjectReaderProvider│      │   ObjectWriterProvider│           │
│   │   ObjectReaderBaseModule      │   ObjectWriterBaseModule        │
│   └───────────────────────┘      └───────────────────────┘           │
│   ┌─────────────┐  ┌──────────────┐  ┌───────────────────┐          │
│   │ Annotations  │  │   Filters    │  │     Utilities     │          │
│   │  (6 files)  │  │  (19 files)  │  │  BeanUtils       │          │
│   │  @JSONField │  │  ValueFilter │  │  TypeUtils       │          │
│   │  @JSONType  │  │  NameFilter  │  │  FieldInfo       │          │
│   └─────────────┘  └──────────────┘  │  internal.asm    │          │
│                                      └───────────────────┘          │
└────────────────────────────────────────────────────────────────────┘
```

### 1. JSON API Layer

The public-facing API that users interact with:

- **`JSON`** - Interface with static entry points (`parseObject`, `parseArray`, `toJSONString`, plus byte-array output overloads)
- **`JSONObject`** / **`JSONArray`** - `LinkedHashMap<String, Object>` and `ArrayList<Object>` subclasses that keep insertion order
- **`JSONFactory`** - Owns the default providers, creates read/write contexts, keeps thread-local creator and provider overrides
- **`JSONReader`** / **`JSONWriter`** (abstract) - Define the parsing and serialization contracts; `JSONReader.of(...)` selects the concrete parser
- **`TypeReference`** / **`SymbolTable`** - Generic type capture; field-name interning for reuse
- **`PropertyNamingStrategy`** / **`JSONException`** - Name conversion; base runtime exception

### 2. Reader Layer (Parsing / Deserialization)

| Class | Input | Notes |
|-------|-------|-------|
| `JSONReaderUTF8` | UTF-8 `byte[]` | Byte-level scanner with character-classification lookup tables |
| `JSONReaderUTF16` | UTF-16 `byte[]`, `char[]`, `String` | Used for text and UTF-16 inputs |
| `JSONReaderASCII` | ASCII / ISO-8859-1 `byte[]` | Fast path for ASCII-only content |

`JSONReader.of(...)` picks the implementation: UTF-8 goes to `JSONReaderUTF8`, ASCII or ISO-8859-1 to `JSONReaderASCII`, UTF-16 and character input to `JSONReaderUTF16`.

### 3. Writer Layer (Serialization)

| Class | Output | Notes |
|-------|--------|-------|
| `JSONWriterUTF8` | UTF-8 `byte[]` | Used for byte-array output |
| `JSONWriterUTF16` | UTF-16 `String` | Used for `toJSONString` |

`JSONWriterUTF16` has runtime variants selected by JDK version: `JSONWriterUTF16JDK8` / `JSONWriterUTF16JDK8UF` for JDK 8, and `JSONWriterUTF16` / `JSONWriterUTF16JDK9UF` for JDK 9+ (the `UF` variants use `Unsafe` field access).

### 4. Object Mapping Layer

#### Reader Package (`com.alibaba.fastjson2.reader`)

| Class | Purpose |
|-------|---------|
| `ObjectReader<T>` | Interface for type-specific deserialization |
| `ObjectReaderCreator` | Builds `ObjectReader` instances; uses `LambdaMetafactory` for constructor and field access |
| `ObjectReaderProvider` | Owns the reader cache and registered modules; resolves types, handles AutoType |
| `ObjectReaderBaseModule` | Registers built-in readers for JDK and common types |
| `ObjectReaders` | Factory helpers for common reader shapes |
| `ObjectReaderException` | Exception type for deserialization failures |

The creator produces per-field reader objects via `createFieldReaders(...)` and wraps them in the generated `ObjectReader`; field-level readers are an internal detail, not a named public type.

#### Writer Package (`com.alibaba.fastjson2.writer`)

| Class | Purpose |
|-------|---------|
| `ObjectWriter<T>` | Interface for type-specific serialization |
| `ObjectWriterCreator` | Builds `ObjectWriter` instances with `LambdaMetafactory`-based field access |
| `ObjectWriterProvider` | Owns the writer cache and registered modules |
| `ObjectWriterBaseModule` | Registers built-in writers for JDK and common types |
| `ObjectWriters` | Factory helpers for common writer shapes |
| `ObjectWriterException` | Exception type for serialization failures |

The `com.alibaba.fastjson2.modules` package defines the extension interfaces used above: `ObjectReaderModule`, `ObjectWriterModule`, `ObjectCodecProvider`, `ObjectReaderAnnotationProcessor`, and `ObjectWriterAnnotationProcessor`.

### 5. Creator Selection Strategy

The providers choose how type-specific readers and writers are created:

```
fastjson2.creator property → "reflect" | "lambda" | "asm" (default "asm")
Standard JDK (8+)          → LambdaMetafactory-based accessors, JIT enabled
Dalvik-based runtime or    → JIT disabled (detected via JDKUtils)
GraalVM native image
SafeMode                   → AutoType resolution disabled
```

- `JSONFactory.CREATOR` reads the `fastjson2.creator` system property; the provider switch handles `"reflect"`, `"lambda"`, and `"asm"` alike, and every branch resolves to the same `ObjectReaderCreator` / `ObjectWriterCreator` class.
- `ObjectReaderCreator.JIT` is enabled unless the runtime is Dalvik-based or GraalVM (both detected by `JDKUtils`); JIT mode builds constructor suppliers and typed functions through `LambdaMetafactory` with a trusted lookup.
- SafeMode, enabled through the `fastjson.parser.safeMode` or `fastjson2.parser.safeMode` property, makes `ObjectReaderProvider` return null for unresolvable AutoType names.

### 6. Annotation Layer (`com.alibaba.fastjson2.annotation`)

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@JSONField` | Method, Field, Parameter | Field-level config (name, format, features, ordinal) |
| `@JSONType` | Type | Class-level config (naming, ignores, features, ordering) |
| `@JSONCreator` | Method, Constructor | Marks a deserialization constructor or factory method |
| `@JSONBuilder` | Type | Marks a builder class for builder-based deserialization |
| `@JSONCompiler` | Type, Method, Constructor | Compiler hint for reader/writer generation |
| `@JSONCompiled` | Any | Marks types with precompiled readers/writers |

### 7. Filter Layer (`com.alibaba.fastjson2.filter`)

The filter package contains 19 classes:

- Serialization filters: `AfterFilter`, `BeforeFilter`, `BeanContext`, `CompositeLabelFilter`, `CompositePropertyFilter`, `CompositePropertyPreFilter`, `ContextNameFilter`, `ContextValueFilter`, `Filter`, `LabelFilter`, `Labels`, `NameFilter`, `PascalNameFilter`, `PropertyFilter`, `PropertyPreFilter`, `SimplePropertyPreFilter`, `ValueFilter`
- Deserialization filters: `ContextAutoTypeBeforeHandler` (AutoType validation), `ExtraProcessor` (extra properties)

## Key Design Patterns

### Factory Pattern
- `JSONFactory` creates read/write contexts and owns the default providers
- `ObjectReaderCreator` / `ObjectWriterCreator` create type-specific readers and writers

### Provider Pattern
- `ObjectReaderProvider` / `ObjectWriterProvider` cache readers and writers in `ConcurrentMap`s, each own a `CopyOnWriteArrayList` of modules, and create on first access

### Strategy Pattern
- `JSONReader` / `JSONWriter` implementations for different encodings and output formats
- The `fastjson2.creator` property selects the creator strategy (all values resolve to one creator class in this build)

### Module Pattern
- `ObjectReaderModule` / `ObjectWriterModule` register custom type handlers and annotation processors
- The base modules register built-in readers and writers for JDK and common types

## Performance Optimizations

### 1. LambdaMetafactory Accessors
- `ObjectReaderCreator` / `ObjectWriterCreator` build constructor suppliers and field accessors through `LambdaMetafactory` with a trusted lookup, avoiding per-call reflection

### 2. Symbol Table
- `SymbolTable` interns field names on first encounter and reuses them across parses, cutting allocation and GC pressure

### 3. Thread-Local Creators and Providers
- `JSONFactory` keeps thread-local `ObjectReaderCreator`, `ObjectReaderProvider`, and `ObjectWriterCreator` slots so per-thread overrides never touch the shared defaults

### 4. Parser Fast Paths
- Byte classification tables in `JSONReaderUTF8`, an ASCII-only shortcut in `JSONReaderASCII`, and JDK-version-specific `JSONWriterUTF16` variants

## Thread Safety

| Component | Thread-Safe? | Notes |
|-----------|:---:|-------|
| `JSON` static methods | Yes | Main entry point; delegates to providers |
| `JSONObject` / `JSONArray` | No | Like `HashMap` / `ArrayList` |
| `JSONReader` / `JSONWriter` | No | Create per operation |
| `ObjectReader` / `ObjectWriter` | Yes | After initialization |
| `ObjectReaderProvider` / `ObjectWriterProvider` | Yes | `ConcurrentMap` caches, `CopyOnWriteArrayList` modules |
| `JSONFactory` | Yes | Static defaults plus thread-local overrides |

## Extension Points

### 1. Module System
- `ObjectReaderModule` / `ObjectWriterModule` (plus `ObjectCodecProvider` and the two annotation processors) are registered via `JSONFactory.getDefaultObjectReaderProvider().register(module)` and the writer provider equivalent

### 2. Custom ObjectReader / ObjectWriter
- Implement `ObjectReader<T>` / `ObjectWriter<T>` and register with `ObjectReaderProvider.register(Type, ObjectReader)` / `ObjectWriterProvider.register(Type, ObjectWriter)`

### 3. Filters
- Passed to `toJSONString` overloads for value transformation, renaming, and property filtering per call

### 4. MixIn Annotations
- `ObjectReaderProvider.mixIn(target, mixinSource)` / `ObjectWriterProvider.mixIn(target, mixinSource)` inject annotations on third-party classes without modifying their source

### 5. AutoType Handlers
- `JSONReader.autoTypeFilter(...)` whitelists types per call; `ObjectReaderProvider.addAutoTypeAccept(String)` extends the global accept list; `ContextAutoTypeBeforeHandler` validates type names before resolution

## Module Dependencies

The repository contains a single Maven module, `core` (JDK 8+), with no internal dependencies; everything else comes from the root POM.

## Build System

- **Build tool**: Maven via the `mvnw` wrapper; multi-module layout with one module (`core`)
- **Java baseline**: JDK 8 (`maven.compiler.source` / `target` = 8)
- **ASM**: A self-contained ASM implementation is embedded at `com.alibaba.fastjson2.internal.asm` for bytecode-level support
- **Testing**: JUnit 5 (via the JUnit BOM, version 5.13.4)
- **Code style**: Checkstyle (`src/checkstyle/fastjson2-checks.xml`); modernizer checks (`src/violations.xml`)
- **CI**: GitHub Actions runs the suite on JDK 8/11/17/21/25 across Ubuntu, Windows, and macOS (JDK 25 excluded on macOS), in both the default creator mode and the `fastjson2.creator=reflect` mode

## Documentation

- [Features Reference](features_en.md) - All `JSONReader` / `JSONWriter` features (Chinese: features_cn.md)
- [Annotations Guide](annotations_en.md) - `@JSONField`, `@JSONType`, `@JSONCreator` (Chinese: annotations_cn.md)
- [Reader Design](design_jsonreader_en.md) / [Writer Design](design_jsonwriter_en.md) - Layer internals (Chinese: design_jsonreader_cn.md, design_jsonwriter_cn.md)
- [AutoType Security](autotype_en.md) - AutoType mechanism and configuration (Chinese: autotype_cn.md)
- [MixIn Annotations](mixin_en.md) - Inject annotations on third-party classes (Chinese: mixin_cn.md)
- [Custom Reader/Writer](register_custom_reader_writer_en.md) - Implement custom `ObjectReader` / `ObjectWriter` (Chinese: register_custom_reader_writer_cn.md)
- [Filter System](Filter/index_en.md) - Serialization filters (Chinese: Filter/index_cn.md)
- [JSONType @seealso](jsontype_seealso_en.md) - Polymorphic type configuration (Chinese: jsontype_seealso_cn.md)
- [Performance Guide](performance_en.md) - Tuning tips (Chinese: performance_cn.md)
- [FAQ](FAQ_en.md) - Frequently asked questions (Chinese: FAQ_cn.md)
- [v1 to v2 Migration](fastjson_1_upgrade_en.md) - Upgrade guide (Chinese: fastjson_1_upgrade_cn.md)
