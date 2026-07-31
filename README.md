##### Language: English | [中文](README_cn.md)

# FASTJSON 2 (Trimmed)

A trimmed fork of FASTJSON 2.0.63 that keeps only the core module: JSON text protocol parsing and serialization, the `JSONObject` / `JSONArray` tree model, and JavaBean binding. Runs on JDK 8 and later. The artifact is `com.alibaba.fastjson2:fastjson2`, version `2.0.63`.

## Feature Scope

- JSON text protocol parsing and serialization (`String` / `byte[]` input, UTF-8 / UTF-16)
- `JSONObject` / `JSONArray` tree model
- JavaBean deserialization and serialization (ASM and reflection implementations)
- Annotations: `@JSONField`, `@JSONType`, `@JSONCreator`, `@JSONCompiled`, and more
- Serialization filters (`NameFilter`, `ValueFilter`, `PropertyFilter`, and more)
- `AutoType` deserialization support (off by default)

## Table of Contents

- [Quick Start](#quick-start)
- [1. Installation](#1-installation)
- [2. Basic Usage](#2-basic-usage)
- [3. Advanced Usage](#3-advanced-usage)
- [4. Note for Fastjson 1.x Users](#4-note-for-fastjson-1x-users)
- [5. Documentation](#5-documentation)
- [6. Contributing](#6-contributing)

## Quick Start

Add the dependency and start parsing:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.63</version>
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

**Maven:**

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.63</version>
</dependency>
```

**Gradle:**

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2:2.0.63'
}
```

# 2. Basic Usage

The package name is `com.alibaba.fastjson2`. All `JSONWriter.Feature` and `JSONReader.Feature` values are off by default.

## 2.1 Parse to `JSONObject`

```java
String text = "{\"id\":1,\"name\":\"fastjson2\"}";
JSONObject data = JSON.parseObject(text);
```

`byte[]` input:

```java
byte[] bytes = ...;
JSONObject data = JSON.parseObject(bytes);
```

## 2.2 Parse to `JSONArray`

```java
String text = "[1,2,3]";
JSONArray data = JSON.parseArray(text);
```

## 2.3 Parse to a JavaBean

```java
String text = "{\"id\":1,\"name\":\"fastjson2\"}";
User user = JSON.parseObject(text, User.class);
```

## 2.4 Parse to an Arbitrary Type

```java
Object value = JSON.parse("{\"id\":1}");       // JSONObject
Object value = JSON.parse("[1,2,3]");          // JSONArray
Object value = JSON.parse("1.5");              // BigDecimal
```

## 2.5 Serialize

```java
String text = JSON.toJSONString(obj);          // String
byte[] bytes = JSON.toJSONBytes(obj);          // byte[]
```

`JSONObject` / `JSONArray` instance methods:

```java
String text = obj.toJSONString();
String text = obj.toJSONString(JSONWriter.Feature.PrettyFormat);
```

## 2.6 `JSONObject` / `JSONArray` Operations

Read properties:

```java
JSONObject obj = JSON.parseObject("{\"id\":2,\"name\":\"fastjson2\",\"enable\":true}");

int id = obj.getIntValue("id");
String name = obj.getString("name");
boolean enable = obj.getBooleanValue("enable");
```

Read nested structures:

```java
JSONArray array = obj.getJSONArray("items");
JSONObject child = obj.getJSONObject("child");
```

Modify:

```java
obj.put("key", value);
obj.remove("key");
obj.containsKey("key");
```

`Map` / `List` semantics:

`JSONObject` extends `Map<String, Object>` and `JSONArray` extends `List<Object>`, so the collection APIs work directly.

# 3. Advanced Usage

## 3.1 Features Configuration

`JSONWriter.Feature` and `JSONReader.Feature` control serialization and deserialization behavior. All features are off by default.

```java
// Serialization with features
String json = JSON.toJSONString(user,
    JSONWriter.Feature.WriteNulls,
    JSONWriter.Feature.PrettyFormat);

// Deserialization with features
User user = JSON.parseObject(json, User.class,
    JSONReader.Feature.SupportSmartMatch);
```

See the full [Features Reference](docs/features_en.md).

## 3.2 Annotations

Use `@JSONField` and `@JSONType` to customize serialization and deserialization:

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

## 3.3 Custom Serializer / Deserializer

Implement `ObjectWriter<T>` or `ObjectReader<T>` for custom serialization logic:

```java
class MoneyWriter implements ObjectWriter<Money> {
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Money money = (Money) object;
        jsonWriter.writeString(money.getCurrency() + " " + money.getAmount());
    }
}

JSON.register(Money.class, new MoneyWriter());
```

See the full [Custom Reader/Writer Guide](docs/register_custom_reader_writer_en.md).

## 3.4 Filters

Filters transform the serialized output:

| Filter | Purpose |
|--------|---------|
| `ValueFilter` | Transform property values |
| `NameFilter` | Rename properties |
| `PropertyFilter` | Include or exclude properties conditionally |
| `AfterFilter` / `BeforeFilter` | Inject additional content |
| `LabelFilter` | Scenario-based serialization |
| `ContextValueFilter` / `ContextNameFilter` | Context-aware transformations |

See the full [Filter Documentation](docs/Filter/index_en.md).

# 4. Note for Fastjson 1.x Users

The package name is `com.alibaba.fastjson2`, different from the `com.alibaba.fastjson` package used by Fastjson 1.x. Features are off by default and `AutoType` is disabled by default.

See the [Migration Guide](docs/fastjson_1_upgrade_en.md) for API differences.

# 5. Documentation

| Document | Description |
|----------|-------------|
| [index.md](docs/index.md) | Overview of this trimmed fork |
| [Features Reference](docs/features_en.md) | `JSONReader` / `JSONWriter` feature list |
| [Annotations Guide](docs/annotations_en.md) | `@JSONField`, `@JSONType`, `@JSONCreator` usage |
| [Custom Reader/Writer](docs/register_custom_reader_writer_en.md) | Implement `ObjectReader` / `ObjectWriter` |
| [Filter System](docs/Filter/index_en.md) | Serialization filters |
| [AutoType Security](docs/autotype_en.md) | AutoType mechanism and configuration |
| [MixIn Annotations](docs/mixin_en.md) | Annotations on third-party classes |
| [Performance Guide](docs/performance_en.md) | Tuning tips |
| [Migration Guide](docs/fastjson_1_upgrade_en.md) | Upgrading from Fastjson 1.x |
| [Architecture](docs/ARCHITECTURE.md) | Internal design |
| [FAQ](docs/FAQ_en.md) | Frequently asked questions |

# 6. Contributing

Bug reports and pull requests are welcome.
