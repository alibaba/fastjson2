[![Java CI](https://img.shields.io/github/workflow/status/alibaba/fastjson2/Java%20CI/main?logo=github&logoColor=white)](https://github.com/alibaba/fastjson2/actions/workflows/ci.yaml)
[![Codecov](https://codecov.io/gh/alibaba/fastjson2/branch/master/graph/badge.svg)](https://codecov.io/gh/alibaba/fastjson2/branch/master)
[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba.fastjson2/fastjson2?logo=apache-maven&logoColor=white)](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2)
[![GitHub release](https://img.shields.io/github/release/alibaba/fastjson2)](https://github.com/alibaba/fastjson2/releases)
[![License](https://img.shields.io/github/license/alibaba/fastjson2?color=4D7A97&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-ready--to--code-green?label=gitpod&logo=gitpod&logoColor=white)](https://gitpod.io/#https://github.com/alibaba/fastjson2)

# 1. FASTJSON v2

`FASTJSON v2`是`FASTJSON`项目的重要升级，目标是为下一个十年提供一个高性能的`JSON`库：

- 同一套`API`支持`JSON/JSONB`两种协议，`JSONPath`是一等公民；
- 支持全量解析和部分解析；
- 支持`Java`服务端、客户端`Android`、大数据场景。

相关文档：

- `JSONB`格式文档：
  https://github.com/alibaba/fastjson2/wiki/jsonb_format_cn
- `FASTJSON v2`性能有了很大提升，具体性能数据看这里：  
  https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark

# 2. 使用前准备

## 2.1 `Maven`依赖

在`fastjson v2`中，`groupId`和`1.x`不一样，是`com.alibaba.fastjson2`：

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.1</version>
</dependency>
```

可以在 [maven.org](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2) 查看最新可用的版本。

## 2.2 `fastjson v1`的兼容包

如果原来使用`fastjson 1.2.x`版本，可以使用兼容包，兼容包不能保证100%兼容，请仔细测试验证，发现问题请及时反馈。

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.1</version>
</dependency>
```

## 2.3 常用类和方法

在`fastjson v2`中，`package`和`1.x`不一样，是`com.alibaba.fastjson2`。如果你之前用的是`fastjson1`，大多数情况直接更包名就即可。

```java
package com.alibaba.fastjson2;

class JSON {
    // 将字符串解析成JSONObject
    static JSONObject parseObject(String str);

    // 将字符串解析成JSONArray
    static JSONArray parseArray(String str);

    // 将字符串解析成Java对象
    static T parseObject(byte[] utf8Bytes, Class<T> objectClass);

    // 将Java对象输出成字符串
    static String toJSONString(Object object);

    // 将Java对象输出成UT8编码的byte[]
    static byte[] toJSONBytes(Object object);
}

class JSONB {
    // 将jsonb格式的byte[]解析成Java对象
    static T parseObject(byte[] jsonbBytes, Class<T> objectClass);

    // 将Java对象输出成jsonb格式的byte[]
    static byte[] toBytes(Object object);
}

class JSONObject {
    Object get(String key);
    int getIntValue(String key);
    Integer getInteger(String key);
    long getLongValue(String key);
    Long getLong(String key);
    T getObject(String key, Class<T> objectClass);

    // 将JSONObject对象转换为Java对象
    T toJavaObject(Class<T> objectClass);
}

class JSONArray {
    Object get(int index);
    int getIntValue(int index);
    Integer getInteger(int index);
    long getLongValue(int index);
    Long getLong(int index);
    T getObject(int index, Class<T> objectClass);
}

class JSONPath {
    // 构造JSONPath
    static JSONPath of(String path);

    // 根据path直接解析输入，会部分解析优化，不会全部解析
    Object extract(JSONReader jsonReader);

    // 根据path对对象求值
    Object eval(Object rootObject);
}

class JSONReader {
    // 构造基于String输入的JSONReader
    static JSONReader of(String str);

    // 构造基于ut8编码byte数组输入的JSONReader
    static JSONReader of(byte[] utf8Bytes);

    // 构造基于char[]输入的JSONReader
    static JSONReader of(char[] chars);

    // 构造基于json格式byte数组输入的JSONReader
    static JSONReader ofJSONB(byte[] jsonbBytes)
}
```

# 3. 读取`JSON`对象

```java
String str = "{\"id\":123}";
JSONObject jsonObject = JSON.parseObject(str);
int id = jsonObject.getIntValue("id");
```

```java
String str = "[\"id\", 123]";
JSONArray jsonArray = JSON.parseArray(str);
String name = jsonArray.getString(0);
int id = jsonArray.getIntValue(1);
```

# 4. 将`JavaBean`对象生成`JSON`

## 4.1 将`JavaBean`对象生成`JSON`格式的字符串

```java
class Product {
    public int id;
    public String name;
}

Product product = new Product();
product.id = 1001;
product.name = "DataWorks";

JSON.toJSONString(product);

// 生成如下的结果
{
    "id"   : 1001,
    "name" : "DataWorks"
}

JSON.toJSONString(product, JSONWriter.Feature.BeanToArray);
// 生成如下的结果
[123, "DataWorks"]
```

## 4.2 将`JavaBean`对象生成`UTF8`编码的`byte[]`

```java
Product product = ...;
byte[] utf8JSONBytes = JSON.toJSONBytes(product);
```

## 4.3 将`JavaBean`对象生成`JSONB`格式的`byte[]`

```java
Product product = ...;
byte[] jsonbBytes = JSONB.toBytes(product);

byte[] jsonbBytes = JSONB.toBytes(product, JSONWriter.Feature.BeanToArray);
```

# 5. 读取`JavaBean`

## 5.1 将字符串读取成`JavaBean`

```java
String str = "{\"id\":123}";
Product product = JSON.parseObject(str, Product.class);
```

## 5.2 将`UTF8`编码的`byte[]`读取成`JavaBean`

```java
byte[] utf8Bytes = "{\"id\":123}".getBytes(StandardCharsets.UTF_8);
Product product = JSON.parseObject(utf8Bytes, Product.class);
```

## 5.3 将`JSONB`数据读取成`JavaBean`

```java
byte[] jsonbBytes = ...
Product product = JSONB.parseObject(jsonbBytes, Product.class);

Product product = JSONB.parseObject(jsonbBytes, Product.class, JSONReader.Feature.SupportBeanArrayMapping);
```

# 6. 使用`JSONPath`

## 6.1 使用`JSONPath`部分读取数据

```java
String str = ...;

JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.of(str);
Object result = path.extract(parser);
```

## 6.2 使用`JSONPath`读取部分`utf8Bytes`的数据

```java
byte[] utf8Bytes = ...;

JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.of(utf8Bytes);
Object result = path.extract(parser);
```

## 6.3 使用`JSONPath`读取部分`jsonbBytes`的数据

```java
byte[] jsonbBytes = ...;

JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.ofJSONB(jsonbBytes); // 注意，这是利用ofJSONB方法
Object result = path.extract(parser);
```
