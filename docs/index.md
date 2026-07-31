# FASTJSON2 精简版

基于 `FASTJSON2 2.0.63` 裁剪的 JSON 库，仅保留 `JSON` 文本协议（`JSON/JSONB` 中已移除 `JSONB`）与核心树 API。面向需要纯 `JSON` 解析/序列化的场景，无多余模块。

## 功能范围

- `JSON` 文本协议解析与序列化（`String` / `byte[]` 输入，UTF-8 / UTF-16）
- `JSONObject` / `JSONArray` 树模型
- `JavaBean` 反序列化与序列化（含 ASM / 反射两种实现）
- 注解：`@JSONField`、`@JSONType`、`@JSONCreator`、`@JSONCompiled` 等
- 序列化过滤器（`NameFilter` / `ValueFilter` / `PropertyFilter` 等）
- `AutoType` 反序列化支持（默认关闭）

已移除：`JSONB` 二进制协议、`JSONPath`、`JSON Schema`、`CSV`、`Kotlin` 模块、`Spring` 扩展、`Android`、`fastjson1-compatible` 兼容层、JMH benchmark。

## 1. 添加依赖

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.63</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2:2.0.63'
}
```

## 2. 解析

### 2.1 解析为 `JSONObject`

```java
String text = "{\"id\":1,\"name\":\"fastjson2\"}";
JSONObject data = JSON.parseObject(text);
```

`byte[]` 输入：

```java
byte[] bytes = ...;
JSONObject data = JSON.parseObject(bytes);
```

### 2.2 解析为 `JSONArray`

```java
String text = "[1,2,3]";
JSONArray data = JSON.parseArray(text);
```

### 2.3 解析为 `JavaBean`

```java
String text = "{\"id\":1,\"name\":\"fastjson2\"}";
User user = JSON.parseObject(text, User.class);
```

### 2.4 解析为任意类型

```java
Object value = JSON.parse("{\"id\":1}");       // JSONObject
Object value = JSON.parse("[1,2,3]");          // JSONArray
Object value = JSON.parse("1.5");              // BigDecimal
```

## 3. 序列化

```java
String text = JSON.toJSONString(obj);          // String
byte[] bytes = JSON.toJSONBytes(obj);          // byte[]
```

`JSONObject` / `JSONArray` 实例方法：

```java
String text = obj.toJSONString();
String text = obj.toJSONString(JSONWriter.Feature.PrettyFormat);
```

## 4. `JSONObject` / `JSONArray` 使用

### 4.1 读取属性

```java
JSONObject obj = JSON.parseObject("{\"id\":2,\"name\":\"fastjson2\",\"enable\":true}");

int id = obj.getIntValue("id");
String name = obj.getString("name");
boolean enable = obj.getBooleanValue("enable");
```

### 4.2 读取嵌套结构

```java
JSONArray array = obj.getJSONArray("items");
JSONObject child = obj.getJSONObject("child");
```

### 4.3 修改

```java
obj.put("key", value);
obj.remove("key");
obj.containsKey("key");
```

### 4.4 `Map` / `List` 语义

`JSONObject` 继承自 `Map<String, Object>`，`JSONArray` 继承自 `List<Object>`，可直接使用集合 API。

## 5. 进阶

| 主题 | 文档 |
|------|------|
| 序列化特性 | [features_cn.md](features_cn.md) |
| 注解 | [annotations_cn.md](annotations_cn.md) |
| 自定义序列化器 | [register_custom_reader_writer_cn.md](register_custom_reader_writer_cn.md) |
| 过滤器 | [Filter/index_cn.md](Filter/index_cn.md) |
| 性能优化 | [performance_cn.md](performance_cn.md) |
