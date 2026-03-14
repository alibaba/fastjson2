[![Java CI](https://img.shields.io/github/actions/workflow/status/alibaba/fastjson2/ci.yaml?branch=main&logo=github&logoColor=white)](https://github.com/alibaba/fastjson2/actions/workflows/ci.yaml)
[![Codecov](https://img.shields.io/codecov/c/github/alibaba/fastjson2/main?logo=codecov&logoColor=white)](https://codecov.io/gh/alibaba/fastjson2/branch/main)
[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba.fastjson2/fastjson2?logo=apache-maven&logoColor=white)](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2)
[![GitHub release](https://img.shields.io/github/release/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/releases)
[![Java support](https://img.shields.io/badge/Java-8+-green?logo=java&logoColor=white)](https://openjdk.java.net/)
[![License](https://img.shields.io/github/license/alibaba/fastjson2?color=4D7A97&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-ready--to--code-green?label=gitpod&logo=gitpod&logoColor=white)](https://gitpod.io/#https://github.com/alibaba/fastjson2)
[![Last SNAPSHOT](https://img.shields.io/nexus/snapshots/https/central.sonatype.org/com.alibaba.fastjson2/fastjson2?label=latest%20snapshot)](https://central.sonatype.com/repository/maven-snapshots/com/alibaba/fastjson2/fastjson2/maven-metadata.xml)
[![GitHub Stars](https://img.shields.io/github/stars/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/fork)
[![user repos](https://badgen.net/github/dependents-repo/alibaba/fastjson2?label=user%20repos)](https://github.com/alibaba/fastjson2/network/dependents)
[![GitHub Contributors](https://img.shields.io/github/contributors/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/graphs/contributors)

##### 语言: [English](README.md) | 中文
##### 本项目的Issues会被同步沉淀至[阿里云开发者社区](https://developer.aliyun.com/ask/)

# FASTJSON 2

**FASTJSON 2** 是一个性能极致并且简单易用的 Java JSON 库，是 FASTJSON 项目的重要升级，目标是为未来十年提供一个高性能的 JSON 库。

![fastjson logo](https://user-images.githubusercontent.com/1063891/233821110-0c912009-4de3-4664-a27e-25274f2fa9c1.jpg)

## 核心特性

- **极致性能** - 性能远超 Jackson、Gson、org.json 等流行 JSON 库。 [性能数据](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)
- **双格式支持** - 原生支持 JSON（文本）和 [JSONB（二进制）](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_cn)两种协议
- **全量/部分解析** - 支持全量解析和通过 [JSONPath](https://alibaba.github.io/fastjson2/JSONPath/jsonpath_cn) 进行选择性提取（兼容 SQL:2016 标准）
- **现代 Java** - 深度优化 JDK 8/11/17/21，支持 compact string、Record 和 Vector API
- **多平台** - 适用于 Java 服务端、Android 8+ 客户端及大数据应用
- **Kotlin 原生** - 一等公民级 [Kotlin 扩展](https://alibaba.github.io/fastjson2/Kotlin/kotlin_cn)，提供惯用的 DSL 风格 API
- **JSON Schema** - 内置高性能[校验支持](https://alibaba.github.io/fastjson2/JSONSchema/json_schema_cn)
- **安全优先** - AutoType 默认关闭，无硬编码白名单，支持 SafeMode
- **GraalVM 就绪** - 兼容 GraalVM Native Image

## 目录

- [快速开始](#快速开始)
- [添加依赖](#1-添加依赖)
  - [核心库](#11-核心库)
  - [Fastjson v1 兼容模块](#12-fastjson-v1-兼容模块)
  - [Kotlin 模块](#13-kotlin-模块)
  - [Spring 框架集成](#14-spring-框架集成)
- [简单使用](#2-简单使用)
  - [解析为 JSONObject](#21-将-json-解析为-jsonobject)
  - [解析为 JSONArray](#22-将-json-解析为-jsonarray)
  - [解析为 Java 对象](#23-将-json-解析为-java-对象)
  - [序列化为 JSON](#24-将-java-对象序列化为-json)
  - [JSONObject 与 JSONArray](#25-使用-jsonobject-和-jsonarray)
  - [序列化 JavaBean](#26-将-javabean-序列化为-json)
- [进阶使用](#3-进阶使用)
  - [JSONB 二进制格式](#31-jsonb-二进制格式)
  - [JSONPath](#32-jsonpath)
  - [Feature 配置](#33-feature-配置)
  - [注解](#34-注解)
  - [自定义序列化/反序列化](#35-自定义序列化反序列化)
  - [过滤器](#36-过滤器)
- [从 Fastjson 1.x 升级](#4-从-fastjson-1x-升级)
- [文档索引](#5-文档索引)
- [参与贡献](#6-参与贡献)

## 快速开始

添加依赖，即刻开始解析 JSON：

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.61</version>
</dependency>
```

```java
import com.alibaba.fastjson2.JSON;

// 解析
User user = JSON.parseObject("{\"name\":\"张三\",\"age\":25}", User.class);

// 序列化
String json = JSON.toJSONString(user);
```

# 1. 添加依赖

## 1.1 核心库

`FASTJSON 2` 的 `groupId` 与 1.x 不同，为 `com.alibaba.fastjson2`：

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

> 可以在 [Maven Central](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2) 查看最新可用版本。

## 1.2 Fastjson v1 兼容模块

如果原来使用 `fastjson 1.2.x` 版本，可以使用兼容包作为直接替换。兼容包不能保证 100% 兼容，请仔细测试验证，发现问题请及时[反馈](https://github.com/alibaba/fastjson2/issues)。

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

## 1.3 Kotlin 模块

如果项目使用 Kotlin，可以使用 `fastjson2-kotlin` 模块，提供惯用的 Kotlin 扩展函数：

**Maven:**

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-kotlin</artifactId>
    <version>2.0.61</version>
</dependency>
```

酌情添加标准库（kotlin-stdlib）和反射库（kotlin-reflect）。若使用数据类（data class）或通过构造函数传入参数，则需添加反射库：

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

## 1.4 Spring 框架集成

如果项目使用 Spring 框架，请使用对应版本的扩展模块。完整配置请参考 [Spring 集成指南](docs/Spring/spring_support_cn.md)。

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
    // 根据 Spring 版本选择：
    implementation 'com.alibaba.fastjson2:fastjson2-extension-spring5:2.0.61'
    // 或
    implementation 'com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.61'
}
```

# 2. 简单使用

> `FASTJSON 2` 的 `package` 与 1.x 不同，为 `com.alibaba.fastjson2`。从 v1 升级时只需修改包名导入即可。

### 2.1 将 JSON 解析为 `JSONObject`

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

### 2.2 将 JSON 解析为 `JSONArray`

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

### 2.3 将 JSON 解析为 Java 对象

**Java:**

```java
String text = "{\"id\":1,\"name\":\"张三\"}";
User user = JSON.parseObject(text, User.class);
```

**Kotlin:**

```kotlin
import com.alibaba.fastjson2.*

val text = """{"id":1,"name":"张三"}"""
val user = text.to<User>()          // User
val user = text.parseObject<User>() // User（另一种写法）
```

### 2.4 将 Java 对象序列化为 JSON

**Java:**

```java
User user = new User(1, "张三");
String text = JSON.toJSONString(user);   // String 输出
byte[] bytes = JSON.toJSONBytes(user);   // byte[] 输出
```

**Kotlin:**

```kotlin
import com.alibaba.fastjson2.*

val user = User(1, "张三")
val text = user.toJSONString()      // String
val bytes = user.toJSONByteArray()  // ByteArray
```

### 2.5 使用 `JSONObject` 和 `JSONArray`

#### 2.5.1 获取简单属性

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

#### 2.5.2 读取 JavaBean

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

#### 2.5.3 转为 JavaBean

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

### 2.6 将 JavaBean 序列化为 JSON

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

输出结果：

```json
{"id":2,"name":"FastJson2"}
```

# 3. 进阶使用

### 3.1 JSONB 二进制格式

JSONB 是一种高性能的二进制 JSON 格式，提供更快的序列化/反序列化速度和更小的数据体积。详见 [JSONB 格式规范](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_cn)。

#### 序列化为 JSONB

```java
User user = ...;
byte[] bytes = JSONB.toBytes(user);
byte[] bytes = JSONB.toBytes(user, JSONWriter.Feature.BeanToArray); // 更紧凑
```

#### 解析 JSONB

```java
byte[] bytes = ...;
User user = JSONB.parseObject(bytes, User.class);
User user = JSONB.parseObject(bytes, User.class, JSONReader.Feature.SupportArrayToBean);
```

### 3.2 JSONPath

JSONPath 支持不完全反序列化即可从 JSON 文档中提取特定字段，非常适合从大型数据中提取部分数据。FASTJSON 2 实现了 [SQL:2016](https://en.wikipedia.org/wiki/SQL:2016) JSONPath 语法。

#### 从 String 中提取

```java
String text = ...;
JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.of(text);
Object result = path.extract(parser);
```

#### 从 byte[] 中提取

```java
byte[] bytes = ...;
JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.of(bytes);
Object result = path.extract(parser);
```

#### 从 JSONB byte[] 中提取

```java
byte[] bytes = ...;
JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.ofJSONB(bytes); // 注意这里使用 ofJSONB 方法
Object result = path.extract(parser);
```

完整的过滤表达式、聚合函数、数组切片等用法请参阅 [JSONPath 文档](https://alibaba.github.io/fastjson2/JSONPath/jsonpath_cn)。

### 3.3 Feature 配置

FASTJSON 2 通过 `JSONWriter.Feature` 和 `JSONReader.Feature` 提供对序列化和反序列化行为的精细控制。所有 Feature **默认关闭**。

```java
// 带 Feature 的序列化
String json = JSON.toJSONString(user,
    JSONWriter.Feature.WriteNulls,
    JSONWriter.Feature.PrettyFormat);

// 带 Feature 的反序列化
User user = JSON.parseObject(json, User.class,
    JSONReader.Feature.SupportSmartMatch);
```

完整的 Feature 列表和从 fastjson 1.x 的迁移映射请参阅 [Feature 参考文档](docs/features_cn.md)。

### 3.4 注解

使用 `@JSONField` 和 `@JSONType` 自定义序列化/反序列化行为：

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

详见 [注解使用指南](docs/annotations_cn.md)。

### 3.5 自定义序列化/反序列化

实现 `ObjectWriter<T>` 或 `ObjectReader<T>` 以自定义序列化逻辑：

```java
// 自定义 Writer
class MoneyWriter implements ObjectWriter<Money> {
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Money money = (Money) object;
        jsonWriter.writeString(money.getCurrency() + " " + money.getAmount());
    }
}

// 注册
JSON.register(Money.class, new MoneyWriter());
```

详见 [自定义 Reader/Writer 指南](docs/register_custom_reader_writer_cn.md)。

### 3.6 过滤器

FASTJSON 2 提供了完善的序列化过滤器体系：

| 过滤器 | 用途 |
|--------|------|
| `ValueFilter` | 转换属性值 |
| `NameFilter` | 重命名属性 |
| `PropertyFilter` | 条件性包含/排除属性 |
| `AfterFilter` / `BeforeFilter` | 注入额外内容 |
| `LabelFilter` | 基于场景的序列化 |
| `ContextValueFilter` / `ContextNameFilter` | 上下文感知转换 |

详见 [过滤器文档](docs/Filter/index_cn.md)。

# 4. 从 Fastjson 1.x 升级

FASTJSON 2 提供兼容模式（直接替换）和新 API 模式两种升级方式。关键变化：

| 方面 | Fastjson 1.x | Fastjson 2.x |
|------|-------------|-------------|
| 包名 | `com.alibaba.fastjson` | `com.alibaba.fastjson2` |
| GroupId | `com.alibaba` | `com.alibaba.fastjson2` |
| AutoType | 默认通过白名单开启 | 默认关闭（更安全） |
| 循环引用检测 | 默认开启 | 默认关闭 |
| 智能匹配 | 默认开启 | 默认关闭 |
| 默认 Feature | 多个 Feature 默认开启 | 所有 Feature 默认关闭 |

完整的分步说明、API 映射表和常见问题请参阅 [升级指南](docs/fastjson_1_upgrade_cn.md)。

# 5. 文档索引

### 核心参考

| 文档 | 说明 |
|------|------|
| [Feature 参考](docs/features_cn.md) | JSONReader/JSONWriter Feature 完整列表 |
| [注解指南](docs/annotations_cn.md) | @JSONField、@JSONType、@JSONCreator 使用说明 |
| [架构文档](docs/ARCHITECTURE.md) | 内部架构、设计模式和类层次结构 |
| [常见问题](docs/FAQ_cn.md) | 常见问题与排查指南 |

### 格式与协议

| 文档 | 说明 |
|------|------|
| [JSONB 格式](https://alibaba.github.io/fastjson2/JSONB/jsonb_format_cn) | 二进制 JSON 格式规范 |
| [JSONB vs Hessian/Kryo](docs/JSONB/jsonb_vs_hessian_kryo_cn.md) | 与其他二进制格式的性能对比 |
| [JSONB 大小对比](docs/JSONB/jsonb_size_compare_cn.md) | 数据体积对比 |
| [CSV 支持](docs/csv_cn.md) | CSV 读写支持 |

### JSONPath

| 文档 | 说明 |
|------|------|
| [JSONPath 指南](docs/JSONPath/jsonpath_cn.md) | 语法、操作符和示例 |
| [多值 JSONPath](docs/JSONPath/jsonpath_multi_cn.md) | 多值提取 |
| [类型化 JSONPath](docs/JSONPath/jsonpath_typed_cn.md) | 类型安全的 JSONPath 提取 |
| [JSONPath 性能](docs/JSONPath/jsonpath_benchmark_cn.md) | 性能数据 |

### 框架集成

| 文档 | 说明 |
|------|------|
| [Spring 支持](docs/Spring/spring_support_cn.md) | Spring MVC、WebFlux、Data Redis、Messaging |
| [Kotlin 扩展](docs/Kotlin/kotlin_cn.md) | Kotlin API 和 DSL |
| [Android 支持](docs/Android/android_benchmark_cn.md) | Android 8+ 集成 |

### 自定义

| 文档 | 说明 |
|------|------|
| [自定义 Reader/Writer](docs/register_custom_reader_writer_cn.md) | 实现 ObjectReader/ObjectWriter |
| [MixIn 注解](docs/mixin_cn.md) | 为第三方类注入注解 |
| [AutoType 安全](docs/autotype_cn.md) | AutoType 机制和安全配置 |
| [JSON Schema](docs/JSONSchema/json_schema_cn.md) | Schema 校验 |
| [过滤器](docs/Filter/index_cn.md) | 序列化过滤器 |

### 升级与性能

| 文档 | 说明 |
|------|------|
| [v1 到 v2 升级](docs/fastjson_1_upgrade_cn.md) | 升级指南与 API 映射 |
| [性能优化指南](docs/performance_cn.md) | 调优建议与最佳实践 |
| [性能测试](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark) | 完整性能测试结果 |

# 6. 参与贡献

我们欢迎各种形式的贡献——Bug 报告、功能请求、文档改进和代码贡献。

- 参阅 [CONTRIBUTING.md](CONTRIBUTING.md) 了解开发环境搭建、编码规范和 PR 流程
- 参阅 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) 了解社区行为准则
- 参阅 [SECURITY.md](SECURITY.md) 了解安全漏洞报告流程

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=alibaba/fastjson2&type=Date)](https://star-history.com/#alibaba/fastjson2)
