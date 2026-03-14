# Frequently Asked Questions

## General

### What is the difference between FASTJSON and FASTJSON 2?

FASTJSON 2 is a complete rewrite of the original FASTJSON library, designed for the next decade. Key differences:

- **New package**: `com.alibaba.fastjson2` (v1 was `com.alibaba.fastjson`)
- **Better performance**: Significant improvements across all operations
- **More secure**: AutoType disabled by default, no hardcoded whitelist
- **Modern Java**: JDK 11/17/21 optimizations, Record support, Vector API
- **Binary format**: Native JSONB support
- **JSON Schema**: Built-in validation

### Can FASTJSON 2 coexist with FASTJSON 1.x?

Yes. Because they use different package names (`com.alibaba.fastjson2` vs `com.alibaba.fastjson`), both can exist in the same project. However, using the compatibility module (`com.alibaba:fastjson:2.x`) will conflict with FASTJSON 1.x since they share the same package.

### What Java versions are supported?

- **Core library**: Java 8+
- **Full feature set**: Java 11+ (for `core-jdk11` module with compact string optimizations)
- **Vector API optimizations**: Java 17+
- **Android**: Android 8+ (API level 26+)

### Does FASTJSON 2 support GraalVM Native Image?

Yes. FASTJSON 2 is compatible with GraalVM Native Image. Note that ASM-based code generation is not available in native images, so the reflection-based or lambda-based creator will be used instead.

## Parsing & Deserialization

### How do I parse JSON with unknown structure?

```java
// Parse as JSONObject (for JSON objects)
JSONObject obj = JSON.parseObject(jsonString);

// Parse as JSONArray (for JSON arrays)
JSONArray arr = JSON.parseArray(jsonString);

// Parse as generic Object (auto-detect)
Object result = JSON.parse(jsonString);
```

### How do I parse JSON with generic types?

Use `TypeReference` for complex generic types:

```java
// List<User>
List<User> users = JSON.parseObject(json, new TypeReference<List<User>>(){});

// Map<String, List<User>>
Map<String, List<User>> map = JSON.parseObject(json,
    new TypeReference<Map<String, List<User>>>(){});
```

### How do I handle date/time formats?

Use `@JSONField(format = "...")` or global configuration:

```java
// Per-field
public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date eventTime;
}

// Global
JSON.configReaderDateFormat("yyyy-MM-dd");

// Per-call
User user = JSON.parseObject(json, User.class, "yyyy-MM-dd");
```

### How do I handle case-insensitive field matching?

Enable `SupportSmartMatch` to automatically handle camelCase, PascalCase, snake_case, and kebab-case:

```java
User user = JSON.parseObject(json, User.class, JSONReader.Feature.SupportSmartMatch);
```

> Note: Smart matching is **off** by default in FASTJSON 2 (it was on by default in v1).

### How do I use BigDecimal for floating-point numbers?

```java
JSONObject obj = JSON.parseObject(json,
    JSONReader.Feature.UseBigDecimalForFloats,
    JSONReader.Feature.UseBigDecimalForDoubles);
```

### How do I trim string values during parsing?

```java
User user = JSON.parseObject(json, User.class, JSONReader.Feature.TrimString);
```

## Serialization

### How do I include null fields in JSON output?

```java
String json = JSON.toJSONString(user, JSONWriter.Feature.WriteNulls);
```

For specific null handling strategies:

```java
// null String → ""
JSONWriter.Feature.WriteNullStringAsEmpty

// null List → []
JSONWriter.Feature.WriteNullListAsEmpty

// null Number → 0
JSONWriter.Feature.WriteNullNumberAsZero

// null Boolean → false
JSONWriter.Feature.WriteNullBooleanAsFalse
```

### How do I pretty-print JSON?

```java
String json = JSON.toJSONString(user, JSONWriter.Feature.PrettyFormat);
```

### How do I handle large Long values for JavaScript compatibility?

JavaScript cannot handle Java `long` values beyond `Number.MAX_SAFE_INTEGER` (2^53 - 1). Use `BrowserCompatible` or `WriteLongAsString`:

```java
// Auto-detect and convert large numbers to strings
String json = JSON.toJSONString(user, JSONWriter.Feature.BrowserCompatible);

// Always serialize Long as String
String json = JSON.toJSONString(user, JSONWriter.Feature.WriteLongAsString);
```

### How do I serialize fields in a specific order?

Use `@JSONField(ordinal = N)` or `@JSONType(orders = {...})`:

```java
public class User {
    @JSONField(ordinal = 1)
    public String name;

    @JSONField(ordinal = 2)
    public int age;
}

// or at class level
@JSONType(orders = {"name", "age", "email"})
public class User { ... }
```

### How do I exclude specific fields from serialization?

Multiple approaches:

```java
// Annotation-based
@JSONField(serialize = false)
public String password;

// Class-level ignore
@JSONType(ignores = {"password", "secretKey"})
public class User { ... }

// Filter-based
PropertyFilter filter = (object, name, value) -> !"password".equals(name);
String json = JSON.toJSONString(user, filter);
```

## AutoType & Security

### Is FASTJSON 2 secure by default?

Yes. Unlike FASTJSON 1.x, FASTJSON 2 disables AutoType by default. JSON data containing `@type` fields will be ignored unless you explicitly enable AutoType.

### How do I enable AutoType safely?

Use `AutoTypeFilter` with a narrow whitelist instead of enabling global AutoType:

```java
// Preferred: scoped filter with narrow whitelist
Filter autoTypeFilter = JSONReader.autoTypeFilter(
    "com.mycompany.model"  // Only allow classes in this package
);

Object result = JSON.parseObject(json, Object.class, autoTypeFilter);
```

If you must enable global AutoType (not recommended for internet-facing services):

```java
Object result = JSON.parseObject(json, Object.class, JSONReader.Feature.SupportAutoType);
```

### What is SafeMode?

SafeMode completely disables AutoType, even if explicitly configured in the code. Enable it with a JVM parameter:

```
-Dfastjson2.parser.safeMode=true
```

## Performance

### How do I get the best parsing performance?

1. **Use byte[] input** when possible - `JSON.parseObject(bytes, Type.class)` avoids String conversion overhead.
2. **Reuse JSONPath instances** - `JSONPath.of("$.id")` is cacheable and thread-safe.
3. **Use partial parsing** - For large documents, use JSONPath to extract only what you need.
4. **Use JSONB** - For internal service communication, JSONB is significantly faster than text JSON.
5. **Avoid unnecessary features** - Each enabled feature adds a small overhead.

### How do I get the best serialization performance?

1. **Use byte[] output** - `JSON.toJSONBytes(obj)` is faster than `JSON.toJSONString(obj)` for most use cases.
2. **Use BeanToArray** - `JSONWriter.Feature.BeanToArray` produces smaller output and is faster to serialize.
3. **Use JSONB** - For binary protocols, JSONB is significantly faster.

### How does FASTJSON 2 compare to Jackson and Gson?

FASTJSON 2 consistently outperforms Jackson and Gson in benchmarks. See the detailed benchmark data at [fastjson_benchmark](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark).

## Spring Integration

### How do I replace Jackson with FASTJSON 2 in Spring Boot?

Add the extension dependency and configure the message converter. See the full [Spring Integration Guide](Spring/spring_support_en.md).

Quick setup for Spring 6.x:

```java
@Configuration
public class JsonConfig extends WebMvcConfigurationSupport {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converters.add(0, converter);
    }
}
```

### How do I use FASTJSON 2 with Spring Data Redis?

Use `GenericFastJsonRedisSerializer`:

```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setDefaultSerializer(new GenericFastJsonRedisSerializer());
    return template;
}
```

## Migration from Fastjson 1.x

### What is the easiest way to migrate?

Use the compatibility package as a drop-in replacement:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.61</version>
</dependency>
```

This provides the same package name (`com.alibaba.fastjson`) with FASTJSON 2's engine underneath.

### What changed with `ParserConfig.getGlobalInstance().addAccept()`?

In FASTJSON 2, use `ObjectReaderProvider`:

```java
JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("com.mycompany.xxx");
```

### What replaces `ObjectSerializer` and `ObjectDeserializer`?

| Fastjson 1.x | Fastjson 2.x |
|---------------|-------------|
| `ObjectSerializer` | `ObjectWriter` |
| `ObjectDeserializer` | `ObjectReader` |
| `SerializerFeature` | `JSONWriter.Feature` |
| `Feature` (parser) | `JSONReader.Feature` |

See the full [Migration Guide](fastjson_1_upgrade_en.md) for the complete API mapping table.

## Troubleshooting

### I'm getting `com.alibaba.fastjson2.JSONException`

Common causes:
1. **Malformed JSON** - Validate your JSON with a linter.
2. **Type mismatch** - The JSON structure doesn't match the target Java type.
3. **Missing default constructor** - The target class needs a no-arg constructor (or use `@JSONCreator`).
4. **Enum mismatch** - Enable `ErrorOnEnumNotMatch` to get detailed error messages.

### Fields are not being serialized

Check for:
1. Fields must be `public` or have public getter methods (unless using `FieldBased` feature).
2. `transient` fields are skipped by default.
3. Check if `@JSONField(serialize = false)` or `@JSONType(ignores = ...)` is applied.

### Fields are not being deserialized

Check for:
1. JSON key names must match Java field names (or use `@JSONField(name = "...")` to map).
2. Enable `SupportSmartMatch` if the JSON uses a different naming convention.
3. Fields must be `public` or have public setter methods (unless using `FieldBased` feature).

### Getting `ClassNotFoundException` or `AutoType` errors

FASTJSON 2 disables AutoType by default. If your JSON contains `@type` fields:
1. Use `AutoTypeFilter` with a narrow whitelist for specific classes.
2. Or enable `SupportAutoType` (not recommended for untrusted input).
3. Do not use AutoType in internet-facing services.
