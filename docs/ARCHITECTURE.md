# Fastjson2 Architecture

## Overview

Fastjson2 is a high-performance JSON library for Java, designed as the next-generation version of Fastjson. It supports both JSON and JSONB (binary JSON) formats, with optimized parsing and serialization capabilities.

## Project Structure

```
fastjson2/
├── core                    # Core library
├── core-jdk11              # JDK 11+ specific extensions
├── extension               # Extension modules
├── extension-jaxrs         # JAX-RS integration
├── extension-solon         # Solon framework integration
├── extension-spring5       # Spring 5 integration
├── extension-spring6       # Spring 6 integration
├── fastjson1-compatible    # Fastjson 1.x compatibility layer
├── kotlin                  # Kotlin support module
├── codegen                 # Code generation tools
├── benchmark               # Performance benchmarks
└── docs                    # Documentation
```

## Core Module Architecture

### Main Components

The core module (`com.alibaba.fastjson2`) consists of the following key components:

#### 1. JSON API Layer
- **`JSON.java`** - Main entry point providing static methods for parsing and serialization
- **`JSONObject.java`** - JSON object implementation (Map-like structure)
- **`JSONArray.java`** - JSON array implementation (List-like structure)
- **`JSONB.java`** - Binary JSON format support

#### 2. Reader Layer (Parsing)
- **`JSONReader.java`** - Abstract reader for JSON parsing
- **`JSONReaderUTF8.java`** - UTF-8 encoding JSON parser
- **`JSONReaderUTF16.java`** - UTF-16 encoding JSON parser
- **`JSONReaderASCII.java`** - ASCII optimized parser
- **`JSONReaderJSONB.java`** - Binary JSON (JSONB) parser

#### 3. Writer Layer (Serialization)
- **`JSONWriter.java`** - Abstract writer for JSON serialization
- **`JSONWriterUTF8.java`** - UTF-8 encoding JSON writer
- **`JSONWriterUTF16.java`** - UTF-16 encoding JSON writer
- **`JSONWriterJSONB.java`** - Binary JSON (JSONB) writer

#### 4. JSONPath Layer
- **`JSONPath.java`** - JSONPath query implementation
- **`JSONPathSegment.java`** - Path segment handling
- **`JSONPathParser.java`** - JSONPath expression parser
- **`JSONPathFilter.java`** - Filter operations for JSONPath
- **`JSONPathSegmentIndex.java`** - Array index segment
- **`JSONPathSegmentName.java`** - Object property segment

#### 5. Object Mapping Layer

##### Reader Package (`com.alibaba.fastjson2.reader`)
- **`ObjectReader.java`** - Interface for object deserialization
- **`ObjectReaderProvider.java`** - Reader instance management
- **`FieldReader.java`** - Field-level deserialization
- **`ObjectReaderBaseModule.java`** - Base module for reader extensions
- **`ObjectReaderCreator.java`** - Factory for creating readers
- **`ObjectReaderCreatorASM.java`** - ASM-based optimized reader creation
- **`ObjectReaderCreatorLambda.java`** - Lambda-based reader creation
- **`ObjectReaderAdapter.java`** - Adapter pattern for readers

##### Writer Package (`com.alibaba.fastjson2.writer`)
- **`ObjectWriter.java`** - Interface for object serialization
- **`ObjectWriterProvider.java`** - Writer instance management
- **`FieldWriter.java`** - Field-level serialization
- **`ObjectWriterBaseModule.java`** - Base module for writer extensions
- **`ObjectWriterCreator.java`** - Factory for creating writers
- **`ObjectWriterCreatorASM.java`** - ASM-based optimized writer creation

##### Introspect Package (`com.alibaba.fastjson2.introspect`)
- **`BeanUtils.java`** - JavaBean introspection utilities
- **`FieldInfo.java`** - Field metadata
- **`TypeUtils.java`** - Type handling utilities

#### 6. Annotation Layer (`com.alibaba.fastjson2.annotation`)
- **`JSONField.java`** - Field-level configuration annotation
- **`JSONType.java`** - Class-level configuration annotation
- **`JSONCompiler.java`** - Compile-time code generation annotation

#### 7. Filter Layer (`com.alibaba.fastjson2.filter`)
- **`ValueFilter.java`** - Value transformation filter
- **`NameFilter.java`** - Property name transformation filter
- **`PropertyFilter.java`** - Property inclusion filter
- **`ContextValueFilter.java`** - Context-aware value filter
- **`ContextNameFilter.java`** - Context-aware name filter
- **`AutoTypeBeforeHandler.java`** - Auto-type handling before parsing

#### 8. Schema Layer (`com.alibaba.fastjson2.schema`)
- **`JSONSchema.java`** - JSON Schema validation

#### 9. Support Package (`com.alibaba.fastjson2.support`)
- **`csv/`** - CSV parsing and writing support
- **`geo/`** - GeoJSON support
- **`retrofit/`** - Retrofit integration

## Key Design Patterns

### 1. Factory Pattern
- `JSONFactory` - Creates reader/writer instances
- `ObjectReaderCreator` - Creates object readers
- `ObjectWriterCreator` - Creates object writers

### 2. Provider Pattern
- `ObjectReaderProvider` - Manages reader instances with caching
- `ObjectWriterProvider` - Manages writer instances with caching

### 3. Strategy Pattern
- `JSONReader` implementations for different encodings
- `JSONWriter` implementations for different encodings
- `ObjectReaderCreator` variants (ASM, Lambda, Reflect)

### 4. Visitor Pattern
- `JSONPath` segment traversal
- `JSONReader` event-driven parsing

## Performance Optimizations

### 1. ASM Code Generation
- Generates optimized bytecode for object readers/writers at runtime
- Reduces reflection overhead
- Located in `ObjectReaderCreatorASM` and `ObjectWriterCreatorASM`

### 2. Lambda Metafactory
- Uses `LambdaMetafactory` for method handles on JDK 8+
- Alternative to reflection for method invocation
- Located in `ObjectReaderCreatorLambda`

### 3. Vector API (JDK 17+)
- SIMD optimizations for UTF-8/UTF-16 processing
- Located in `JSONReaderUTF8` and `JSONReaderUTF16`

### 4. String Interning
- Symbol table for frequently used strings
- `SymbolTable.java` for efficient string reuse

### 5. Lazy Parsing
- Partial parsing support for large JSON documents
- `JSONPath` for selective data extraction

## JSONB Binary Format

JSONB is a binary representation of JSON with the following characteristics:
- Compact binary encoding
- Schema-less format
- Support for all JSON types plus binary data
- Fast parsing and serialization

Format specification: [JSONB Format Documentation](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en)

## Thread Safety

- `JSONReader` and `JSONWriter` instances are NOT thread-safe
- `ObjectReader` and `ObjectWriter` instances are thread-safe after initialization
- `JSONFactory` provides thread-local instances where appropriate
- Static methods in `JSON` class are thread-safe

## Extension Points

### 1. Module System
- `JSONReader.Module` - Custom deserialization modules
- `JSONWriter.Module` - Custom serialization modules
- `ObjectReaderModule` - Object reader customization
- `ObjectWriterModule` - Object writer customization

### 2. Filters
- Value transformation
- Property name mapping
- Property filtering
- Context-aware processing

### 3. MixIn Annotations
- Configure serialization/deserialization for third-party classes
- `JSON.mixIn()` API

## Security Features

### 1. AutoType Security
- `@type` field handling with whitelist/blacklist
- `AutoTypeBeforeHandler` for custom type checking
- `JSONReader.Feature.SafeMode` for secure parsing

### 2. JSON Schema Validation
- JSON Schema draft support
- Validation before parsing

## Module Dependencies

```
core
  ├── extension (depends on core)
  ├── extension-spring5 (depends on core, extension)
  ├── extension-spring6 (depends on core, extension)
  ├── extension-solon (depends on core, extension)
  ├── extension-jaxrs (depends on core, extension)
  ├── fastjson1-compatible (depends on core)
  ├── kotlin (depends on core)
  └── core-jdk11 (depends on core)
```

## Build System

- Maven-based multi-module project
- Java 8+ baseline (core)
- Java 11+ for core-jdk11 module
- Kotlin support in separate module

## Testing

- JUnit 5 for unit tests
- Test categories organized by issue numbers:
  - `issues/` - General issues
  - `issues_1000/` - Issues 1000-1999
  - `issues_2000/` - Issues 2000-2999
  - etc.

## Documentation

- [Features](https://alibaba.github.io/fastjson2/features_en)
- [JSONB Format](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en)
- [JSONPath](https://alibaba.github.io/fastjson2/JSONPath/json_path_en)
- [Kotlin Support](https://alibaba.github.io/fastjson2/Kotlin/kotlin_en)
- [Benchmarks](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)
