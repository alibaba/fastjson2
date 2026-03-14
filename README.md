[![Java CI](https://img.shields.io/github/actions/workflow/status/alibaba/fastjson2/ci.yaml?branch=main&logo=github&logoColor=white)](https://github.com/alibaba/fastjson2/actions/workflows/ci.yaml)
[![Codecov](https://img.shields.io/codecov/c/github/alibaba/fastjson2/main?logo=codecov&logoColor=white)](https://codecov.io/gh/alibaba/fastjson2/branch/main)
[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba.fastjson2/fastjson2?logo=apache-maven&logoColor=white)](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2)
[![GitHub release](https://img.shields.io/github/release/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/releases)
[![Java support](https://img.shields.io/badge/Java-8+-green?logo=java&logoColor=white)](https://openjdk.java.net/)
[![License](https://img.shields.io/github/license/alibaba/fastjson2?color=4D7A97&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-ready--to--code-green?label=gitpod&logo=gitpod&logoColor=white)](https://gitpod.io/#https://github.com/alibaba/fastjson2)
[![Last SNAPSHOT](https://img.shields.io/nexus/snapshots/https/oss.sonatype.org/com.alibaba.fastjson2/fastjson2?label=latest%20snapshot)](https://oss.sonatype.org/content/repositories/snapshots/com/alibaba/fastjson2/)
[![GitHub Stars](https://img.shields.io/github/stars/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/fork)
[![user repos](https://badgen.net/github/dependents-repo/alibaba/fastjson2?label=user%20repos)](https://github.com/alibaba/fastjson2/network/dependents)
[![GitHub Contributors](https://img.shields.io/github/contributors/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/graphs/contributors)

##### Language: English | [中文](README_cn.md)

# FASTJSON 2

**FASTJSON 2** is a high-performance JSON library for Java, designed as the next-generation successor to FASTJSON with a goal of providing an optimized JSON solution for the next ten years.

![fastjson logo](https://user-images.githubusercontent.com/1063891/233821110-0c912009-4de3-4664-a27e-25274f2fa9c1.jpg)

## Highlights

- **Blazing Fast** - Significantly outperforms Jackson, Gson, and org.json. [Benchmarks](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)
- **Dual Format** - Native support for both JSON (text) and [JSONB (binary)](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en) protocols
- **Full & Partial Parsing** - Complete document parsing or selective extraction via [JSONPath](https://alibaba.github.io/fastjson2/JSONPath/jsonpath_en) (SQL:2016 compatible)
- **Modern Java** - Optimized for JDK 8/11/17/21 with compact string, Record, and Vector API support
- **Multi-Platform** - Works on Java servers, Android 8+ clients, and big data pipelines
- **Kotlin Native** - First-class [Kotlin extensions](https://alibaba.github.io/fastjson2/Kotlin/kotlin_en) with idiomatic DSL-style API
- **JSON Schema** - Built-in [validation support](https://alibaba.github.io/fastjson2/JSONSchema/json_schema_en) with high performance
- **Secure by Default** - AutoType disabled by default; no hardcoded whitelist; SafeMode support
- **GraalVM Ready** - Compatible with GraalVM Native Image

## Table of Contents

- [Quick Start](#quick-start)
- [Installation](#1-installation)
  - [Core Library](#11-core-library)
  - [Fastjson v1 Compatibility](#12-fastjson-v1-compatibility-module)
  - [Kotlin Module](#13-kotlin-module)
  - [Spring Integration](#14-spring-framework-integration)
- [Basic Usage](#2-basic-usage)
  - [Parse JSON to JSONObject](#21-parse-json-to-jsonobject)
  - [Parse JSON to JSONArray](#22-parse-json-to-jsonarray)
  - [Parse JSON to Java Object](#23-parse-json-to-java-object)
  - [Serialize to JSON](#24-serialize-java-object-to-json)
  - [JSONObject & JSONArray](#25-working-with-jsonobject-and-jsonarray)
  - [Serialize JavaBean](#26-serialize-javabean-to-json)
- [Advanced Usage](#3-advanced-usage)
  - [JSONB Binary Format](#31-jsonb-binary-format)
  - [JSONPath](#32-jsonpath)
  - [Features Configuration](#33-features-configuration)
  - [Annotations](#34-annotations)
  - [Custom Serializer/Deserializer](#35-custom-serializerdeserializer)
  - [Filters](#36-filters)
- [Upgrading from Fastjson 1.x](#4-upgrading-from-fastjson-1x)
- [Documentation](#5-documentation)
- [Contributing](#6-contributing)

## Quick Start

Add the dependency and start parsing JSON in seconds:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.61</version>
</dependency>
```

```java
import com.alibaba.fastjson2.JSON;

// Parse
User user = JSON.parseObject("{\"name\":\"John\",\"age\":25}", User.class);

// Serialize
String json = JSON.toJSONString(user);
```

# 1. Installation

## 1.1 Core Library

The `groupId` for FASTJSON 2 is `com.alibaba.fastjson2` (different from 1.x):

**Maven:**

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.61</version>
</dependency>
```

**Gradle:**

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2:2.0.61'
}
```

> Find the latest version on [Maven Central](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2).

## 1.2 Fastjson v1 Compatibility Module

If you are migrating from `fastjson 1.2.x`, you can use the compatibility package as a drop-in replacement. Note that 100% compatibility is not guaranteed - please test thoroughly and [report issues](https://github.com/alibaba/fastjson2/issues).

**Maven:**

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.61</version>
</dependency>
```

**Gradle:**

```groovy
dependencies {
    implementation 'com.alibaba:fastjson:2.0.61'
}
```

## 1.3 Kotlin Module

For projects using Kotlin, the `fastjson2-kotlin` module provides idiomatic Kotlin extensions:

**Maven:**

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-kotlin</artifactId>
    <version>2.0.61</version>
</dependency>
```

Add the Kotlin standard library and reflection library as needed. The reflection library is required when using data classes or constructor-based parameter passing:

```xml
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-stdlib</artifactId>
    <version>${kotlin-version}</version>
</dependency>

<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
    <version>${kotlin-version}</version>
</dependency>
```

**Kotlin Gradle:**

```kotlin
dependencies {
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.61")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
}
```

## 1.4 Spring Framework Integration

For Spring Framework projects, use the appropriate extension module. See the full [Spring Integration Guide](docs/Spring/spring_support_en.md) for details.

**Maven (Spring 5.x):**

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension-spring5</artifactId>
    <version>2.0.61</version>
</dependency>
```

**Maven (Spring 6.x):**

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension-spring6</artifactId>
    <version>2.0.61</version>
</dependency>
```

**Gradle:**

```groovy
dependencies {
    // Choose one based on your Spring version:
    implementation 'com.alibaba.fastjson2:fastjson2-extension-spring5:2.0.61'
    // or
    implementation 'com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.61'
}
```

# 2. Basic Usage

> The package name for FASTJSON 2 is `com.alibaba.fastjson2`. If upgrading from v1, simply update the package imports.

### 2.1 Parse JSON to `JSONObject`

**Java:**

```java
String text = "{\"id\":1,\"name\":\"fastjson2\"}";
JSONObject data = JSON.parseObject(text);

byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
JSONObject data = JSON.parseObject(bytes);
```

**Kotlin:**

```kotlin
import com.alibaba.fastjson2.*

val text = """{"id":1,"name":"fastjson2"}"""
val data = text.parseObject()

val bytes: ByteArray = text.toByteArray()
val data = bytes.parseObject() // JSONObject
```

### 2.2 Parse JSON to `JSONArray`

**Java:**

```java
String text = "[{\"id\":1},{\"id\":2}]";
JSONArray data = JSON.parseArray(text);
```

**Kotlin:**

```kotlin
import com.alibaba.fastjson2.*

val text = """[{"id":1},{"id":2}]"""
val data = text.parseArray() // JSONArray
```

### 2.3 Parse JSON to Java Object

**Java:**

```java
String text = "{\"id\":1,\"name\":\"John\"}";
User user = JSON.parseObject(text, User.class);
```

**Kotlin:**

```kotlin
import com.alibaba.fastjson2.*

val text = """{"id":1,"name":"John"}"""
val user = text.to<User>()          // User
val user = text.parseObject<User>() // User (alternative)
```

### 2.4 Serialize Java Object to JSON

**Java:**

```java
User user = new User(1, "John");
String text = JSON.toJSONString(user);   // String output
byte[] bytes = JSON.toJSONBytes(user);   // byte[] output
```

**Kotlin:**

```kotlin
import com.alibaba.fastjson2.*

val user = User(1, "John")
val text = user.toJSONString()      // String
val bytes = user.toJSONByteArray()  // ByteArray
```

### 2.5 Working with `JSONObject` and `JSONArray`

#### 2.5.1 Get Simple Properties

```java
String text = "{\"id\": 2, \"name\": \"fastjson2\"}";
JSONObject obj = JSON.parseObject(text);

int id = obj.getIntValue("id");
String name = obj.getString("name");
```

```java
String text = "[2, \"fastjson2\"]";
JSONArray array = JSON.parseArray(text);

int id = array.getIntValue(0);
String name = array.getString(1);
```

#### 2.5.2 Get JavaBean from JSON Containers

**Java:**

```java
JSONArray array = ...;
JSONObject obj = ...;

User user = array.getObject(0, User.class);
User user = obj.getObject("key", User.class);
```

**Kotlin:**

```kotlin
val array: JSONArray = ...
val obj: JSONObject = ...

val user = array.to<User>(0)
val user = obj.to<User>("key")
```

#### 2.5.3 Convert JSONObject/JSONArray to JavaBean

**Java:**

```java
JSONObject obj = ...;
JSONArray array = ...;

User user = obj.toJavaObject(User.class);
List<User> users = array.toJavaList(User.class);
```

**Kotlin:**

```kotlin
val obj: JSONObject = ...
val array: JSONArray = ...

val user = obj.to<User>()           // User
val users = array.toList<User>()    // List<User>
```

### 2.6 Serialize JavaBean to JSON

**Java:**

```java
class User {
    public int id;
    public String name;
}

User user = new User();
user.id = 2;
user.name = "FastJson2";

String text = JSON.toJSONString(user);
byte[] bytes = JSON.toJSONBytes(user);
```

**Kotlin:**

```kotlin
class User(
    var id: Int,
    var name: String
)

val user = User(2, "FastJson2")
val text = user.toJSONString()      // String
val bytes = user.toJSONByteArray()  // ByteArray
```

Output:

```json
{"id":2,"name":"FastJson2"}
```

# 3. Advanced Usage

### 3.1 JSONB Binary Format

JSONB is a high-performance binary JSON format that provides significantly faster serialization/deserialization and smaller payload sizes. See the [JSONB Format Specification](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en).

#### Serialize to JSONB

```java
User user = ...;
byte[] bytes = JSONB.toBytes(user);
byte[] bytes = JSONB.toBytes(user, JSONWriter.Feature.BeanToArray); // Even more compact
```

#### Parse JSONB

```java
byte[] bytes = ...;
User user = JSONB.parseObject(bytes, User.class);
User user = JSONB.parseObject(bytes, User.class, JSONReader.Feature.SupportArrayToBean);
```

### 3.2 JSONPath

JSONPath enables partial parsing of JSON documents without full deserialization, which is ideal for extracting specific fields from large payloads. FASTJSON 2 implements [SQL:2016](https://en.wikipedia.org/wiki/SQL:2016) JSONPath syntax.

#### Extract from String

```java
String text = ...;
JSONPath path = JSONPath.of("$.id"); // Cache and reuse for better performance

JSONReader parser = JSONReader.of(text);
Object result = path.extract(parser);
```

#### Extract from byte[]

```java
byte[] bytes = ...;
JSONPath path = JSONPath.of("$.id"); // Cache and reuse for better performance

JSONReader parser = JSONReader.of(bytes);
Object result = path.extract(parser);
```

#### Extract from JSONB byte[]

```java
byte[] bytes = ...;
JSONPath path = JSONPath.of("$.id"); // Cache and reuse for better performance

JSONReader parser = JSONReader.ofJSONB(bytes); // Note: use ofJSONB method
Object result = path.extract(parser);
```

See the full [JSONPath Documentation](https://alibaba.github.io/fastjson2/JSONPath/jsonpath_en) for filter expressions, aggregate functions, array slicing, and more.

### 3.3 Features Configuration

FASTJSON 2 provides fine-grained control over serialization and deserialization behavior through `JSONWriter.Feature` and `JSONReader.Feature`. All features are **OFF by default**.

```java
// Serialization with features
String json = JSON.toJSONString(user,
    JSONWriter.Feature.WriteNulls,
    JSONWriter.Feature.PrettyFormat);

// Deserialization with features
User user = JSON.parseObject(json, User.class,
    JSONReader.Feature.SupportSmartMatch);
```

See the full [Features Reference](docs/features_en.md) for all available options and migration mapping from fastjson 1.x.

### 3.4 Annotations

Use `@JSONField` and `@JSONType` to customize serialization/deserialization behavior:

```java
public class User {
    @JSONField(name = "user_name", ordinal = 1)
    public String name;

    @JSONField(format = "yyyy-MM-dd", ordinal = 2)
    public Date birthday;

    @JSONField(serialize = false)
    public String password;
}
```

See the full [Annotations Guide](docs/annotations_en.md).

### 3.5 Custom Serializer/Deserializer

Implement `ObjectWriter<T>` or `ObjectReader<T>` for custom serialization logic:

```java
// Custom writer
class MoneyWriter implements ObjectWriter<Money> {
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Money money = (Money) object;
        jsonWriter.writeString(money.getCurrency() + " " + money.getAmount());
    }
}

// Register
JSON.register(Money.class, new MoneyWriter());
```

See the full [Custom Reader/Writer Guide](docs/register_custom_reader_writer_en.md).

### 3.6 Filters

FASTJSON 2 provides a comprehensive filter system for serialization:

| Filter | Purpose |
|--------|---------|
| `ValueFilter` | Transform property values |
| `NameFilter` | Rename properties |
| `PropertyFilter` | Include/exclude properties conditionally |
| `AfterFilter` / `BeforeFilter` | Inject additional content |
| `LabelFilter` | Scenario-based serialization |
| `ContextValueFilter` / `ContextNameFilter` | Context-aware transformations |

See the full [Filter Documentation](docs/Filter/index_en.md).

# 4. Upgrading from Fastjson 1.x

FASTJSON 2 provides both a compatibility mode (drop-in replacement) and a new API mode for upgrading. Key changes:

| Aspect | Fastjson 1.x | Fastjson 2.x |
|--------|-------------|-------------|
| Package | `com.alibaba.fastjson` | `com.alibaba.fastjson2` |
| GroupId | `com.alibaba` | `com.alibaba.fastjson2` |
| AutoType | Enabled with whitelist | Disabled by default (more secure) |
| Circular Reference | Detected by default | Not detected by default |
| Smart Match | On by default | Off by default |
| Default Features | Multiple features on | All features off |

See the full [Migration Guide](docs/fastjson_1_upgrade_en.md) for step-by-step instructions, API mapping table, and common issues.

# 5. Documentation

### Core References

| Document | Description |
|----------|-------------|
| [Features Reference](docs/features_en.md) | Complete list of JSONReader/JSONWriter features |
| [Annotations Guide](docs/annotations_en.md) | @JSONField, @JSONType, @JSONCreator usage |
| [Architecture](docs/ARCHITECTURE.md) | Internal architecture, design patterns, and class hierarchy |
| [FAQ](docs/FAQ_en.md) | Frequently asked questions and troubleshooting |

### Format & Protocol

| Document | Description |
|----------|-------------|
| [JSONB Format](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_en) | Binary JSON format specification |
| [JSONB vs Hessian/Kryo](docs/JSONB/jsonb_vs_hessian_kryo_en.md) | Performance comparison with other binary formats |
| [JSONB Size Comparison](docs/JSONB/jsonb_size_compare_en.md) | Payload size comparison |
| [CSV Support](docs/csv_en.md) | CSV reading and writing support |

### JSONPath

| Document | Description |
|----------|-------------|
| [JSONPath Guide](docs/JSONPath/jsonpath_en.md) | Syntax, operators, and examples |
| [Multi-value JSONPath](docs/JSONPath/jsonpath_multi_en.md) | Multi-value extraction |
| [Typed JSONPath](docs/JSONPath/jsonpath_typed_en.md) | Type-safe JSONPath extraction |
| [JSONPath Benchmark](docs/JSONPath/jsonpath_benchmark_en.md) | Performance data |

### Integrations

| Document | Description |
|----------|-------------|
| [Spring Support](docs/Spring/spring_support_en.md) | Spring MVC, WebFlux, Data Redis, Messaging |
| [Kotlin Extensions](docs/Kotlin/kotlin_en.md) | Kotlin API and DSL |
| [Android Support](docs/Android/android_benchmark_en.md) | Android 8+ integration |

### Customization

| Document | Description |
|----------|-------------|
| [Custom Reader/Writer](docs/register_custom_reader_writer_en.md) | Implement ObjectReader/ObjectWriter |
| [MixIn Annotations](docs/mixin_en.md) | Inject annotations on third-party classes |
| [AutoType Security](docs/autotype_en.md) | AutoType mechanism and security configuration |
| [JSON Schema](docs/JSONSchema/json_schema_en.md) | Schema validation |
| [Filter System](docs/Filter/index_en.md) | Serialization filters |

### Migration & Performance

| Document | Description |
|----------|-------------|
| [v1 to v2 Migration](docs/fastjson_1_upgrade_en.md) | Upgrade guide with API mapping |
| [Performance Guide](docs/performance_en.md) | Tuning tips and best practices |
| [Benchmarks](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark) | Comprehensive benchmark results |

# 6. Contributing

We welcome contributions of all kinds - bug reports, feature requests, documentation improvements, and code contributions.

- See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, coding standards, and the pull request process
- See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for community guidelines
- See [SECURITY.md](SECURITY.md) for reporting security vulnerabilities

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=alibaba/fastjson2&type=Date)](https://star-history.com/#alibaba/fastjson2)
