##### 语言: [English](README.md) | 中文

# FASTJSON 2 精简版

基于 `FASTJSON 2.0.63` 裁剪的 JSON 库，仅保留 `core` 模块：`JSON` 文本协议解析与序列化、`JSONObject` / `JSONArray` 树模型以及 `JavaBean` 绑定。要求 JDK 8 及以上。制品坐标为 `com.alibaba.fastjson2:fastjson2`，版本 `2.0.63`。

## 功能范围

- `JSON` 文本协议解析与序列化（`String` / `byte[]` 输入，UTF-8 / UTF-16）
- `JSONObject` / `JSONArray` 树模型
- `JavaBean` 反序列化与序列化（ASM 与反射两种实现）
- 注解：`@JSONField`、`@JSONType`、`@JSONCreator`、`@JSONCompiled` 等
- 序列化过滤器（`NameFilter`、`ValueFilter`、`PropertyFilter` 等）
- `AutoType` 反序列化支持（默认关闭）

## 目录

- [快速开始](#快速开始)
- [1. 添加依赖](#1-添加依赖)
- [2. 基本使用](#2-基本使用)
- [3. 进阶使用](#3-进阶使用)
- [4. 从 Fastjson 1.x 升级](#4-从-fastjson-1x-升级)
- [5. 文档索引](#5-文档索引)
- [6. 参与贡献](#6-参与贡献)

## 快速开始

添加依赖，即刻开始解析 JSON：

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.63</version>
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

# 2. 基本使用

包名为 `com.alibaba.fastjson2`。所有 `JSONWriter.Feature` 与 `JSONReader.Feature` 默认关闭。

## 2.1 解析为 `JSONObject`

```java
String text = "{\"id\":1,\"name\":\"fastjson2\"}";
JSONObject data = JSON.parseObject(text);
```

`byte[]` 输入：

```java
byte[] bytes = ...;
JSONObject data = JSON.parseObject(bytes);
```

## 2.2 解析为 `JSONArray`

```java
String text = "[1,2,3]";
JSONArray data = JSON.parseArray(text);
```

## 2.3 解析为 `JavaBean`

```java
String text = "{\"id\":1,\"name\":\"fastjson2\"}";
User user = JSON.parseObject(text, User.class);
```

## 2.4 解析为任意类型

```java
Object value = JSON.parse("{\"id\":1}");       // JSONObject
Object value = JSON.parse("[1,2,3]");          // JSONArray
Object value = JSON.parse("1.5");              // BigDecimal
```

## 2.5 序列化

```java
String text = JSON.toJSONString(obj);          // String
byte[] bytes = JSON.toJSONBytes(obj);          // byte[]
```

`JSONObject` / `JSONArray` 实例方法：

```java
String text = obj.toJSONString();
String text = obj.toJSONString(JSONWriter.Feature.PrettyFormat);
```

## 2.6 `JSONObject` / `JSONArray` 操作

读取属性：

```java
JSONObject obj = JSON.parseObject("{\"id\":2,\"name\":\"fastjson2\",\"enable\":true}");

int id = obj.getIntValue("id");
String name = obj.getString("name");
boolean enable = obj.getBooleanValue("enable");
```

读取嵌套结构：

```java
JSONArray array = obj.getJSONArray("items");
JSONObject child = obj.getJSONObject("child");
```

修改：

```java
obj.put("key", value);
obj.remove("key");
obj.containsKey("key");
```

`Map` / `List` 语义：

`JSONObject` 继承自 `Map<String, Object>`，`JSONArray` 继承自 `List<Object>`，可直接使用集合 API。

# 3. 进阶使用

## 3.1 Feature 配置

`JSONWriter.Feature` 与 `JSONReader.Feature` 控制序列化和反序列化行为。所有 Feature 默认关闭。

```java
// 带 Feature 的序列化
String json = JSON.toJSONString(user,
    JSONWriter.Feature.WriteNulls,
    JSONWriter.Feature.PrettyFormat);

// 带 Feature 的反序列化
User user = JSON.parseObject(json, User.class,
    JSONReader.Feature.SupportSmartMatch);
```

完整的 Feature 列表请参阅 [Feature 参考文档](docs/features_cn.md)。

## 3.2 注解

使用 `@JSONField` 和 `@JSONType` 自定义序列化 / 反序列化行为：

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

## 3.3 自定义序列化 / 反序列化

实现 `ObjectWriter<T>` 或 `ObjectReader<T>` 以自定义序列化逻辑：

```java
class MoneyWriter implements ObjectWriter<Money> {
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Money money = (Money) object;
        jsonWriter.writeString(money.getCurrency() + " " + money.getAmount());
    }
}

JSON.register(Money.class, new MoneyWriter());
```

详见 [自定义 Reader/Writer 指南](docs/register_custom_reader_writer_cn.md)。

## 3.4 过滤器

过滤器用于转换序列化输出：

| 过滤器 | 用途 |
|--------|------|
| `ValueFilter` | 转换属性值 |
| `NameFilter` | 重命名属性 |
| `PropertyFilter` | 条件性包含 / 排除属性 |
| `AfterFilter` / `BeforeFilter` | 注入额外内容 |
| `LabelFilter` | 基于场景的序列化 |
| `ContextValueFilter` / `ContextNameFilter` | 上下文感知转换 |

详见 [过滤器文档](docs/Filter/index_cn.md)。

# 4. 从 Fastjson 1.x 升级

包名与 1.x 使用的 `com.alibaba.fastjson` 不同，为 `com.alibaba.fastjson2`。所有 Feature 默认关闭，`AutoType` 默认关闭。

API 差异详见 [升级指南](docs/fastjson_1_upgrade_cn.md)。

# 5. 文档索引

| 文档 | 说明 |
|------|------|
| [index.md](docs/index.md) | 本精简版概述 |
| [Feature 参考](docs/features_cn.md) | `JSONReader` / `JSONWriter` Feature 完整列表 |
| [注解指南](docs/annotations_cn.md) | `@JSONField`、`@JSONType`、`@JSONCreator` 使用说明 |
| [自定义 Reader/Writer](docs/register_custom_reader_writer_cn.md) | 实现 `ObjectReader` / `ObjectWriter` |
| [过滤器](docs/Filter/index_cn.md) | 序列化过滤器 |
| [AutoType 安全](docs/autotype_cn.md) | AutoType 机制和安全配置 |
| [MixIn 注解](docs/mixin_cn.md) | 为第三方类注入注解 |
| [性能优化指南](docs/performance_cn.md) | 调优建议 |
| [升级指南](docs/fastjson_1_upgrade_cn.md) | 从 Fastjson 1.x 升级 |
| [架构文档](docs/ARCHITECTURE.md) | 内部设计 |
| [常见问题](docs/FAQ_cn.md) | 常见问题与排查 |

# 6. 参与贡献

欢迎提交 Bug 报告与 Pull Request。
