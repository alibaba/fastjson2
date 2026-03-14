# 常见问题

## 概述

### FASTJSON 和 FASTJSON 2 有什么区别？

FASTJSON 2 是对原始 FASTJSON 库的全面重写，面向未来十年设计。主要区别：

- **新包名**: `com.alibaba.fastjson2`（v1 为 `com.alibaba.fastjson`）
- **更好的性能**: 所有操作均有显著提升
- **更安全**: AutoType 默认关闭，无硬编码白名单
- **现代 Java**: JDK 11/17/21 优化，Record 支持，Vector API
- **二进制格式**: 原生 JSONB 支持
- **JSON Schema**: 内置校验功能

### FASTJSON 2 可以和 FASTJSON 1.x 共存吗？

可以。由于使用不同的包名（`com.alibaba.fastjson2` vs `com.alibaba.fastjson`），两者可以在同一项目中共存。但使用兼容模块（`com.alibaba:fastjson:2.x`）时会与 FASTJSON 1.x 冲突，因为它们共享相同的包名。

### 支持哪些 Java 版本？

- **核心库**: Java 8+
- **完整功能集**: Java 11+（`core-jdk11` 模块提供 compact string 优化）
- **Vector API 优化**: Java 17+
- **Android**: Android 8+（API level 26+）

### FASTJSON 2 支持 GraalVM Native Image 吗？

支持。FASTJSON 2 兼容 GraalVM Native Image。注意在 native image 中 ASM 代码生成不可用，将使用基于反射或 lambda 的创建器。

## 解析与反序列化

### 如何解析结构未知的 JSON？

```java
// 解析为 JSONObject（JSON 对象）
JSONObject obj = JSON.parseObject(jsonString);

// 解析为 JSONArray（JSON 数组）
JSONArray arr = JSON.parseArray(jsonString);

// 解析为通用 Object（自动检测）
Object result = JSON.parse(jsonString);
```

### 如何解析带泛型的 JSON？

使用 `TypeReference` 处理复杂泛型：

```java
// List<User>
List<User> users = JSON.parseObject(json, new TypeReference<List<User>>(){});

// Map<String, List<User>>
Map<String, List<User>> map = JSON.parseObject(json,
    new TypeReference<Map<String, List<User>>>(){});
```

### 如何处理日期/时间格式？

使用 `@JSONField(format = "...")` 或全局配置：

```java
// 字段级别
public class Event {
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public Date eventTime;
}

// 全局配置
JSON.configReaderDateFormat("yyyy-MM-dd");

// 每次调用
User user = JSON.parseObject(json, User.class, "yyyy-MM-dd");
```

### 如何进行大小写不敏感的字段匹配？

启用 `SupportSmartMatch` 可自动处理 camelCase、PascalCase、snake_case 和 kebab-case：

```java
User user = JSON.parseObject(json, User.class, JSONReader.Feature.SupportSmartMatch);
```

> 注意：智能匹配在 FASTJSON 2 中**默认关闭**（v1 中默认开启）。

### 如何使用 BigDecimal 处理浮点数？

```java
JSONObject obj = JSON.parseObject(json,
    JSONReader.Feature.UseBigDecimalForFloats,
    JSONReader.Feature.UseBigDecimalForDoubles);
```

### 如何在解析时去除字符串空格？

```java
User user = JSON.parseObject(json, User.class, JSONReader.Feature.TrimString);
```

## 序列化

### 如何在 JSON 输出中包含 null 字段？

```java
String json = JSON.toJSONString(user, JSONWriter.Feature.WriteNulls);
```

针对特定 null 值的处理策略：

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

### 如何格式化输出 JSON？

```java
String json = JSON.toJSONString(user, JSONWriter.Feature.PrettyFormat);
```

### 如何处理 JavaScript 中的大 Long 值？

JavaScript 无法处理超过 `Number.MAX_SAFE_INTEGER`（2^53 - 1）的 Java `long` 值。使用 `BrowserCompatible` 或 `WriteLongAsString`：

```java
// 自动检测并将大数字转为字符串
String json = JSON.toJSONString(user, JSONWriter.Feature.BrowserCompatible);

// 始终将 Long 序列化为 String
String json = JSON.toJSONString(user, JSONWriter.Feature.WriteLongAsString);
```

### 如何按指定顺序序列化字段？

使用 `@JSONField(ordinal = N)` 或 `@JSONType(orders = {...})`：

```java
public class User {
    @JSONField(ordinal = 1)
    public String name;

    @JSONField(ordinal = 2)
    public int age;
}

// 或在类级别
@JSONType(orders = {"name", "age", "email"})
public class User { ... }
```

### 如何排除特定字段不参与序列化？

多种方式：

```java
// 基于注解
@JSONField(serialize = false)
public String password;

// 类级别忽略
@JSONType(ignores = {"password", "secretKey"})
public class User { ... }

// 基于过滤器
PropertyFilter filter = (object, name, value) -> !"password".equals(name);
String json = JSON.toJSONString(user, filter);
```

## AutoType 与安全

### FASTJSON 2 默认安全吗？

是的。与 FASTJSON 1.x 不同，FASTJSON 2 默认禁用 AutoType。包含 `@type` 字段的 JSON 数据将被忽略，除非显式启用 AutoType。

### 如何安全地启用 AutoType？

使用 `AutoTypeFilter` 配合小范围白名单，而不是启用全局 AutoType：

```java
// 推荐：使用范围有限的过滤器和白名单
Filter autoTypeFilter = JSONReader.autoTypeFilter(
    "com.mycompany.model"  // 仅允许此包下的类
);

Object result = JSON.parseObject(json, Object.class, autoTypeFilter);
```

如果必须启用全局 AutoType（不推荐用于面向互联网的服务）：

```java
Object result = JSON.parseObject(json, Object.class, JSONReader.Feature.SupportAutoType);
```

### 什么是 SafeMode？

SafeMode 完全禁用 AutoType，即使在代码中显式配置也无效。通过 JVM 参数启用：

```
-Dfastjson2.parser.safeMode=true
```

## 性能

### 如何获得最佳解析性能？

1. **尽量使用 byte[] 输入** - `JSON.parseObject(bytes, Type.class)` 可避免 String 转换开销。
2. **复用 JSONPath 实例** - `JSONPath.of("$.id")` 是线程安全的，可缓存重复使用。
3. **使用部分解析** - 对于大型文档，使用 JSONPath 仅提取需要的数据。
4. **使用 JSONB** - 内部服务通信时，JSONB 比文本 JSON 快得多。
5. **避免不必要的 Feature** - 每个启用的 Feature 会增加少量开销。

### 如何获得最佳序列化性能？

1. **使用 byte[] 输出** - `JSON.toJSONBytes(obj)` 在大多数场景下比 `JSON.toJSONString(obj)` 更快。
2. **使用 BeanToArray** - `JSONWriter.Feature.BeanToArray` 产生更小的输出且序列化更快。
3. **使用 JSONB** - 对于二进制协议，JSONB 性能显著更优。

### FASTJSON 2 与 Jackson 和 Gson 相比如何？

FASTJSON 2 在基准测试中始终优于 Jackson 和 Gson。详细数据请参阅 [fastjson_benchmark](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)。

## Spring 集成

### 如何在 Spring Boot 中用 FASTJSON 2 替换 Jackson？

添加扩展依赖并配置消息转换器。详见完整的 [Spring 集成指南](Spring/spring_support_cn.md)。

Spring 6.x 快速配置：

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

### 如何在 Spring Data Redis 中使用 FASTJSON 2？

使用 `GenericFastJsonRedisSerializer`：

```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setDefaultSerializer(new GenericFastJsonRedisSerializer());
    return template;
}
```

## 从 Fastjson 1.x 迁移

### 最简单的迁移方式是什么？

使用兼容包直接替换：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.61</version>
</dependency>
```

这提供与 v1 相同的包名（`com.alibaba.fastjson`），底层使用 FASTJSON 2 引擎。

### `ParserConfig.getGlobalInstance().addAccept()` 如何替换？

在 FASTJSON 2 中，使用 `ObjectReaderProvider`：

```java
JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("com.mycompany.xxx");
```

### `ObjectSerializer` 和 `ObjectDeserializer` 的替代方案是什么？

| Fastjson 1.x | Fastjson 2.x |
|---------------|-------------|
| `ObjectSerializer` | `ObjectWriter` |
| `ObjectDeserializer` | `ObjectReader` |
| `SerializerFeature` | `JSONWriter.Feature` |
| `Feature`（解析器） | `JSONReader.Feature` |

完整的 API 映射表请参阅 [升级指南](fastjson_1_upgrade_cn.md)。

## 故障排查

### 遇到 `com.alibaba.fastjson2.JSONException`

常见原因：
1. **JSON 格式错误** - 使用 JSON 格式检查工具验证您的 JSON。
2. **类型不匹配** - JSON 结构与目标 Java 类型不匹配。
3. **缺少默认构造函数** - 目标类需要无参构造函数（或使用 `@JSONCreator`）。
4. **Enum 不匹配** - 启用 `ErrorOnEnumNotMatch` 获取详细错误信息。

### 字段未被序列化

请检查：
1. 字段必须是 `public` 或有 public getter 方法（除非使用 `FieldBased` Feature）。
2. `transient` 字段默认被跳过。
3. 检查是否应用了 `@JSONField(serialize = false)` 或 `@JSONType(ignores = ...)`。

### 字段未被反序列化

请检查：
1. JSON 键名必须与 Java 字段名匹配（或使用 `@JSONField(name = "...")` 进行映射）。
2. 如果 JSON 使用不同的命名规范，启用 `SupportSmartMatch`。
3. 字段必须是 `public` 或有 public setter 方法（除非使用 `FieldBased` Feature）。

### 遇到 `ClassNotFoundException` 或 AutoType 错误

FASTJSON 2 默认禁用 AutoType。如果您的 JSON 包含 `@type` 字段：
1. 使用 `AutoTypeFilter` 配合窄范围白名单针对特定类。
2. 或启用 `SupportAutoType`（不推荐用于不可信输入）。
3. 不要在面向互联网的服务中使用 AutoType。
