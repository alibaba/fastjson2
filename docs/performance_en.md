# Performance Guide

FASTJSON 2 is designed for maximum performance. This guide covers tuning strategies and best practices to get the most out of the library.

## Performance Architecture

FASTJSON 2 achieves its performance through several key optimizations:

### ASM Code Generation

At runtime, FASTJSON 2 generates optimized bytecode for object readers and writers using ASM. This eliminates reflection overhead for field access and method invocation. The generated code uses switch-case statements on field name hashes for O(1) field lookup during deserialization.

- **When**: First time a type is serialized/deserialized (one-time cost)
- **Where**: `ObjectReaderCreatorASM`, `ObjectWriterCreatorASM`
- **Fallback**: Reflection-based creator when ASM is unavailable (Android, GraalVM Native Image)

### Lambda Metafactory

On JDK 8+, FASTJSON 2 uses `LambdaMetafactory` to create high-performance method handles as an alternative to reflection. This provides near-native-call performance for getter/setter invocations.

### Vector API (JDK 17+)

On JDK 17+, FASTJSON 2 leverages SIMD (Single Instruction, Multiple Data) instructions via the Vector API for bulk character processing in UTF-8 and UTF-16 parsers. This dramatically accelerates string scanning and validation.

### String Interning

The `SymbolTable` provides efficient string interning for frequently used field names, reducing memory allocation and GC pressure during parsing.

### Encoding-Specific Parsers

FASTJSON 2 has dedicated parser implementations for different encodings:
- `JSONReaderUTF8` - optimized for UTF-8 byte streams
- `JSONReaderUTF16` - optimized for UTF-16 (Java String internal representation)
- `JSONReaderASCII` - fast path for ASCII-only content

The library auto-detects the optimal parser based on input type.

## Tuning Strategies

### 1. Prefer byte[] Over String

**Impact: High**

When possible, work with `byte[]` directly instead of `String`:

```java
// Faster: parse from bytes
byte[] bytes = getJsonBytes(); // from network, file, etc.
User user = JSON.parseObject(bytes, User.class);

// Faster: serialize to bytes
byte[] output = JSON.toJSONBytes(user);
```

This avoids the overhead of String encoding/decoding. It is especially effective for HTTP/RPC scenarios where data arrives as bytes.

### 2. Use JSONB for Internal Communication

**Impact: Very High**

For service-to-service communication, JSONB (binary JSON) provides 3-9x faster performance and smaller payload sizes compared to text JSON:

```java
// Serialize
byte[] bytes = JSONB.toBytes(user);

// Deserialize
User user = JSONB.parseObject(bytes, User.class);

// Even faster with BeanToArray
byte[] bytes = JSONB.toBytes(user, JSONWriter.Feature.BeanToArray);
User user = JSONB.parseObject(bytes, User.class, JSONReader.Feature.SupportArrayToBean);
```

### 3. Use JSONPath for Partial Parsing

**Impact: High (for large documents)**

When you only need specific fields from a large JSON document, use JSONPath to extract them without parsing the entire document:

```java
// Cache the JSONPath instance (thread-safe)
static final JSONPath ID_PATH = JSONPath.of("$.id");
static final JSONPath NAME_PATH = JSONPath.of("$.name");

// Extract specific fields
Object id = ID_PATH.extract(JSONReader.of(json));
Object name = NAME_PATH.extract(JSONReader.of(json));
```

### 4. Use BeanToArray for Compact Serialization

**Impact: Medium**

The `BeanToArray` feature serializes objects as JSON arrays instead of objects, removing field name overhead:

```java
// Output: [1,"John",25] instead of {"id":1,"name":"John","age":25}
String json = JSON.toJSONString(user, JSONWriter.Feature.BeanToArray);
User user = JSON.parseObject(json, User.class, JSONReader.Feature.SupportArrayToBean);
```

### 5. Minimize Feature Usage

**Impact: Low-Medium**

Each enabled Feature adds a condition check in the hot path. Only enable features you actually need:

```java
// Good: only enable what you need
String json = JSON.toJSONString(user, JSONWriter.Feature.WriteNulls);

// Avoid: enabling many features "just in case"
String json = JSON.toJSONString(user,
    JSONWriter.Feature.WriteNulls,
    JSONWriter.Feature.PrettyFormat,        // skip if not needed
    JSONWriter.Feature.ReferenceDetection,  // skip if no circular refs
    JSONWriter.Feature.MapSortField);       // skip if order doesn't matter
```

### 6. Use FieldBased for Maximum Speed

**Impact: Medium**

`FieldBased` mode accesses fields directly instead of through getter/setter methods, which is slightly faster:

```java
String json = JSON.toJSONString(user, JSONWriter.Feature.FieldBased);
User user = JSON.parseObject(json, User.class, JSONReader.Feature.FieldBased);
```

> Note: This accesses private fields via reflection/ASM, which may not work in all environments.

### 7. Cache ObjectReader/ObjectWriter for Custom Types

**Impact: Medium**

If you register custom readers/writers, ensure they are singletons:

```java
// Good: singleton pattern
class MoneyWriter implements ObjectWriter<Money> {
    static final MoneyWriter INSTANCE = new MoneyWriter();
    // ...
}
JSON.register(Money.class, MoneyWriter.INSTANCE);
```

## Thread Safety

Understanding thread safety helps avoid unnecessary synchronization:

| Component | Thread-Safe? | Notes |
|-----------|:---:|-------|
| `JSON` static methods | Yes | Main entry point, always safe |
| `JSONObject` / `JSONArray` | No | Not synchronized, like `HashMap`/`ArrayList` |
| `JSONReader` / `JSONWriter` | No | Create per-operation, never share across threads |
| `ObjectReader` / `ObjectWriter` | Yes | After initialization, safe to share |
| `JSONPath` | Yes | Cache and reuse across threads |
| `ObjectReaderProvider` / `ObjectWriterProvider` | Yes | Internal caching is thread-safe |

## JVM Tuning

### Recommended JVM Flags

```
# Enable compact strings (JDK 9+, on by default)
-XX:+CompactStrings

# For JDK 17+, enable Vector API incubator module
--add-modules jdk.incubator.vector
```

### Memory Considerations

- FASTJSON 2 uses thread-local buffers for serialization, which reduces GC pressure but increases per-thread memory.
- For applications with many threads, monitor thread-local buffer usage.
- JSONB typically uses less memory than text JSON due to compact encoding.

## Benchmarking

FASTJSON 2 includes JMH benchmarks in the `benchmark/` module. To run:

```bash
cd benchmark
mvn clean package
java -jar target/benchmarks.jar
```

For published benchmark results comparing FASTJSON 2 with Jackson, Gson, and other libraries, see:
- [Benchmark Results](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)
- [JSONB vs Hessian/Kryo](JSONB/jsonb_vs_hessian_kryo_en.md)
- [Android Benchmarks](Android/android_benchmark_en.md)
