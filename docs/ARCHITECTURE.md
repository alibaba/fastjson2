# Fastjson2 Architecture

## Overview

Fastjson2 is a high-performance JSON library for Java, designed as the next-generation version of Fastjson. It supports both JSON and JSONB (binary JSON) formats, with deeply optimized parsing and serialization capabilities targeting JDK 8 through 21+.

## Project Structure

```
fastjson2/
├── core/                      # Core library (JDK 8+)
├── extension/                 # Base extensions (Arrow, ClickHouse, Geo, Retrofit, etc.)
├── extension-jaxrs/           # JAX-RS integration
├── extension-solon/           # Solon framework integration
├── extension-spring5/         # Spring 5 integration (Web MVC, WebFlux, Data Redis, Messaging)
├── extension-spring6/         # Spring 6 integration
├── fastjson1-compatible/      # Fastjson 1.x API compatibility layer
├── kotlin/                    # Kotlin extension functions and DSL
├── codegen/                   # Compile-time code generation (APT)
├── benchmark/                 # JMH performance benchmarks
├── safemode-test/             # SafeMode test suite
├── android-test/              # Android compatibility tests
├── test-jdk17/                # JDK 17 feature tests (Records, sealed classes)
├── test-jdk25/                # JDK 25 feature tests
└── docs/                      # Documentation
```

## Core Module Architecture

### Component Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        JSON API Layer                            │
│          JSON.java  │  JSONB.java  │  JSONPath.java              │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────────────────┐         ┌─────────────────────┐       │
│   │    Reader Layer      │         │    Writer Layer      │       │
│   │                     │         │                     │       │
│   │  JSONReaderUTF8     │         │  JSONWriterUTF8     │       │
│   │  JSONReaderUTF16    │         │  JSONWriterUTF16    │       │
│   │  JSONReaderASCII    │         │  JSONWriterJSONB    │       │
│   │  JSONReaderJSONB    │         │                     │       │
│   └────────┬────────────┘         └────────┬────────────┘       │
│            │                               │                     │
│   ┌────────┴────────────┐         ┌────────┴────────────┐       │
│   │  Object Mapping      │         │  Object Mapping      │       │
│   │                     │         │                     │       │
│   │  ObjectReader       │         │  ObjectWriter       │       │
│   │  FieldReader        │         │  FieldWriter        │       │
│   │  ObjectReaderCreator│         │  ObjectWriterCreator│       │
│   │    ├── ASM          │         │    ├── ASM          │       │
│   │    └── Lambda/Refl. │         │    └── Lambda/Refl. │       │
│   │                     │         │                     │       │
│   └─────────────────────┘         └─────────────────────┘       │
│                                                                  │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐      │
│   │  Annotations  │  │   Filters    │  │   JSON Schema    │      │
│   │  @JSONField  │  │  ValueFilter │  │   JSONSchema     │      │
│   │  @JSONType   │  │  NameFilter  │  │                  │      │
│   │  @JSONCreator│  │  Property... │  │                  │      │
│   └──────────────┘  └──────────────┘  └──────────────────┘      │
│                                                                  │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐      │
│   │  JSONPath     │  │  Support     │  │  Introspect      │      │
│   │  Segment     │  │  CSV         │  │  BeanUtils       │      │
│   │  Filter      │  │  GeoJSON     │  │  FieldInfo       │      │
│   │  Parser      │  │  Retrofit    │  │  TypeUtils       │      │
│   └──────────────┘  └──────────────┘  └──────────────────┘      │
└──────────────────────────────────────────────────────────────────┘
```

### 1. JSON API Layer

The public-facing API through which users interact with the library:

- **`JSON.java`** - Main entry point with static methods (`parseObject`, `parseArray`, `toJSONString`, `toJSONBytes`)
- **`JSONObject.java`** - JSON object (extends `LinkedHashMap<String, Object>`, maintains insertion order)
- **`JSONArray.java`** - JSON array (extends `ArrayList<Object>`)
- **`JSONB.java`** - Binary JSON format support
- **`JSONPath.java`** - JSONPath query engine with SQL:2016 compatibility
- **`JSONFactory.java`** - Factory for creating reader/writer instances with thread-local caching

### 2. Reader Layer (Parsing / Deserialization)

Encoding-specific parsers that convert input to Java objects:

| Class | Input | Optimization |
|-------|-------|-------------|
| `JSONReaderUTF8` | UTF-8 `byte[]` | SIMD via Vector API (JDK 17+) |
| `JSONReaderUTF16` | UTF-16 `char[]` / `String` | Compact string optimization (JDK 9+) |
| `JSONReaderASCII` | ASCII `byte[]` | Fast path for ASCII-only content |
| `JSONReaderJSONB` | JSONB `byte[]` | Binary format decoder |

The abstract `JSONReader` base class defines the parsing contract. `JSONFactory` selects the appropriate implementation based on input type and JDK version.

### 3. Writer Layer (Serialization)

Encoding-specific writers that convert Java objects to output:

| Class | Output | Notes |
|-------|--------|-------|
| `JSONWriterUTF8` | UTF-8 `byte[]` | Default for `toJSONBytes()` |
| `JSONWriterUTF16` | UTF-16 `String` | Default for `toJSONString()` |
| `JSONWriterJSONB` | JSONB `byte[]` | Binary format encoder |

### 4. Object Mapping Layer

#### Reader Package (`com.alibaba.fastjson2.reader`)

| Class | Purpose |
|-------|---------|
| `ObjectReader<T>` | Interface for type-specific deserialization |
| `ObjectReaderProvider` | Manages reader instances with concurrent caching |
| `FieldReader` | Handles individual field deserialization |
| `ObjectReaderBaseModule` | Base module for reader extensions |
| `ObjectReaderCreator` | Base factory for creating readers (uses LambdaMetafactory internally for field access) |
| `ObjectReaderCreatorASM` | ASM bytecode-generated readers (highest performance) |
| `ObjectReaderAdapter` | Adapter pattern for readers |

#### Writer Package (`com.alibaba.fastjson2.writer`)

| Class | Purpose |
|-------|---------|
| `ObjectWriter<T>` | Interface for type-specific serialization |
| `ObjectWriterProvider` | Manages writer instances with concurrent caching |
| `FieldWriter` | Handles individual field serialization |
| `ObjectWriterBaseModule` | Base module for writer extensions |
| `ObjectWriterCreator` | Base factory for creating writers |
| `ObjectWriterCreatorASM` | ASM bytecode-generated writers (highest performance) |

#### Creator Selection Strategy

FASTJSON 2 selects the optimal ObjectReader/ObjectWriter creator based on the runtime environment:

```
JDK 8 server  → ObjectReaderCreatorASM (best performance)
JDK 17 server → ObjectReaderCreatorASM + Vector API optimizations
Android        → ObjectReaderCreator (LambdaMetafactory-based, no ASM on Android)
GraalVM Native → ObjectReaderCreator (reflection-based, no dynamic bytecode)
SafeMode       → ObjectReaderCreator (reflection-based)
```

### 5. JSONPath Layer

Full JSONPath implementation compatible with SQL:2016:

- **`JSONPath`** - Query compilation and execution
- **`JSONPathSegment`** - Base class for path segments
- **`JSONPathSegmentIndex`** - Array index access (`$[0]`, `$[-1]`)
- **`JSONPathSegmentName`** - Object property access (`$.name`)
- **`JSONPathParser`** - JSONPath expression parser
- **`JSONPathFilter`** - Filter operations (`$[?(@.price > 10)]`)

JSONPath instances are immutable and thread-safe after construction. They should be cached and reused.

### 6. Annotation Layer (`com.alibaba.fastjson2.annotation`)

| Annotation | Target | Purpose |
|------------|--------|---------|
| `@JSONField` | Field, Method, Parameter | Field-level config (name, format, features, ordinal) |
| `@JSONType` | Class, Interface | Class-level config (naming, ignores, features, ordering) |
| `@JSONCreator` | Constructor, Method | Specify deserialization constructor or factory method |
| `@JSONCompiler` | Class | Enable compile-time code generation |

### 7. Filter Layer (`com.alibaba.fastjson2.filter`)

Serialization filters applied during the write phase:

| Filter | Description |
|--------|-------------|
| `ValueFilter` | Transform property values before output |
| `NameFilter` | Rename properties before output |
| `ContextNameFilter` | Context-aware property renaming |
| `ContextValueFilter` | Context-aware value transformation |
| `PropertyFilter` | Conditionally include/exclude properties |
| `PropertyPreFilter` | Pre-serialization property filtering |
| `AfterFilter` | Append content after object serialization |
| `BeforeFilter` | Prepend content before object serialization |
| `LabelFilter` | Label-based selective serialization |
| `AutoTypeBeforeHandler` | Whitelist-based AutoType control for deserialization |

### 8. Schema Layer (`com.alibaba.fastjson2.schema`)

- **`JSONSchema`** - JSON Schema validation (draft support)
- Validates `JSONObject`, JavaBeans, and raw JSON data
- Can be configured via `@JSONField(schema = "...")` annotations

## Key Design Patterns

### Factory Pattern
- `JSONFactory` creates reader/writer instances with thread-local recycling
- `ObjectReaderCreator` / `ObjectWriterCreator` create type-specific readers/writers

### Provider Pattern
- `ObjectReaderProvider` manages reader instances with `ConcurrentHashMap` caching
- `ObjectWriterProvider` manages writer instances with `ConcurrentHashMap` caching
- First access triggers creation; subsequent accesses retrieve from cache

### Strategy Pattern
- `JSONReader` implementations for different encodings (UTF-8, UTF-16, ASCII, JSONB)
- `JSONWriter` implementations for different output formats
- `ObjectReaderCreator` variants (ASM, Lambda, Reflect) selected by environment

### Visitor Pattern
- `JSONPath` segment traversal through document structure
- `JSONReader` token-based parsing

### Module Pattern
- `ObjectReaderModule` / `ObjectWriterModule` allow extensions to register custom type handlers
- Framework integrations (Spring, JAX-RS) use modules to hook into the serialization pipeline

## Performance Optimizations

### 1. ASM Code Generation
- Generates optimized bytecode at runtime for object readers/writers
- Switch-case on field name hash for O(1) field lookup during deserialization
- Eliminates reflection overhead for field access
- Located in `ObjectReaderCreatorASM` and `ObjectWriterCreatorASM`

### 2. Lambda Metafactory
- Uses `LambdaMetafactory` for method handles on JDK 8+
- Near-native-call performance for getter/setter invocations
- Used by `ObjectReaderCreator` and `ObjectWriterCreator` for field access

### 3. Vector API (JDK 17+)
- SIMD optimizations for bulk character processing in UTF-8/UTF-16 parsers
- Accelerates string scanning, whitespace skipping, and validation
- Located in `JSONReaderUTF8` and `JSONReaderUTF16`

### 4. String Interning
- `SymbolTable` for efficient string reuse of field names
- Reduces memory allocation and GC pressure during parsing
- Keys are interned on first encounter and reused on subsequent parses

### 5. Lazy Parsing
- JSONPath supports partial parsing without full document deserialization
- Only the targeted path is parsed; rest of the document is skipped
- Critical for extracting fields from large JSON documents

### 6. Thread-Local Buffers
- `JSONFactory` provides thread-local reader/writer instances
- Buffer recycling reduces allocation overhead in high-throughput scenarios

## JSONB Binary Format

JSONB is a binary representation of JSON with the following design:

- **Compact encoding**: Small integers (-16 to 47) require only 1 byte
- **Schema-less**: No pre-defined schema needed
- **Type-rich**: Supports all JSON types plus binary data
- **Symbol table**: Optional key compression for repeated field names
- **Multi-encoding**: Strings can be stored as UTF-8, UTF-16, ASCII, or GB18030

Format specification: [JSONB Format Documentation](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en)

## Thread Safety

| Component | Thread-Safe? | Notes |
|-----------|:---:|-------|
| `JSON` static methods | Yes | Main entry point |
| `JSONObject` / `JSONArray` | No | Like `HashMap`/`ArrayList` |
| `JSONReader` / `JSONWriter` | No | Create per-operation |
| `ObjectReader` / `ObjectWriter` | Yes | After initialization |
| `JSONPath` | Yes | Immutable after construction |
| `ObjectReaderProvider` / `ObjectWriterProvider` | Yes | ConcurrentHashMap-based |
| `JSONFactory` | Yes | Thread-local instances |

## Extension Points

### 1. Module System
- `ObjectReaderModule` - Register custom deserialization modules
- `ObjectWriterModule` - Register custom serialization modules
- Modules are registered via `JSONFactory.getDefaultObjectReaderProvider().register(module)`

### 2. Custom ObjectReader/ObjectWriter
- Implement `ObjectReader<T>` for custom deserialization logic
- Implement `ObjectWriter<T>` for custom serialization logic
- Register via `JSON.register(Class, ObjectReader/ObjectWriter)`

### 3. Filters
- Apply during serialization for value transformation, name mapping, property filtering
- Register per-call via `JSON.toJSONString(obj, filter)` or globally

### 4. MixIn Annotations
- Inject annotations on third-party classes without modifying source
- `JSON.mixIn(TargetClass.class, MixInClass.class)`

### 5. AutoType Handlers
- `AutoTypeBeforeHandler` - Custom type validation before deserialization
- `JSONReader.autoTypeFilter(...)` - Whitelist-based type filtering per call

## Module Dependencies

```
core (JDK 8+)
├── extension (core)
│   ├── extension-spring5 (core, extension)
│   ├── extension-spring6 (core, extension)
│   ├── extension-solon (core, extension)
│   └── extension-jaxrs (core, extension)
├── fastjson1-compatible (core)
├── kotlin (core)
├── codegen (core)
└── benchmark (core)
```

## Build System

- **Build tool**: Maven with multi-module structure
- **Java baseline**: JDK 8 (core), JDK 11 (core-jdk11)
- **Kotlin**: Version 2.1.20
- **ASM**: Embedded internally in `com.alibaba.fastjson2.internal.asm` (for bytecode generation)
- **Testing**: JUnit 5, Kotest (Kotlin module)
- **Code style**: Checkstyle (`src/checkstyle/fastjson2-checks.xml`)
- **CI**: GitHub Actions across JDK 8/11/17/21/25 on Ubuntu/Windows/macOS

## Documentation

- [Features Reference](features_en.md) - All JSONReader/JSONWriter features
- [Annotations Guide](annotations_en.md) - @JSONField, @JSONType, @JSONCreator
- [JSONB Format](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en) - Binary format specification
- [JSONPath Guide](JSONPath/jsonpath_en.md) - JSONPath syntax and examples
- [Kotlin Support](Kotlin/kotlin_en.md) - Kotlin API extensions
- [Spring Integration](Spring/spring_support_en.md) - Spring Framework setup
- [Performance Guide](performance_en.md) - Tuning tips and best practices
- [FAQ](FAQ_en.md) - Frequently asked questions
- [Benchmarks](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark) - Performance data
