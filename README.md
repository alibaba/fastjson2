
[![Java CI](https://github.com/alibaba/fastjson2/actions/workflows/ci.yaml/badge.svg?branch=master)](https://github.com/alibaba/fastjson2/actions/workflows/ci.yaml)
[![Codecov](https://codecov.io/gh/alibaba/fastjson2/branch/master/graph/badge.svg)](https://codecov.io/gh/alibaba/fastjson2/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alibaba.fastjson2/fastjson2/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alibaba.fastjson2/fastjson2/)
[![GitHub release](https://img.shields.io/github/release/alibaba/fastjson2.svg)](https://github.com/alibaba/fastjson2/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/alibaba/fastjson2)


# 1. FASTJSON 2.0介绍
FASTJSON 2.0是FASTJSON项目的重要升级，目标是为下一个十年提供一个高性能的JSON库，同一套API支持JSON/JSONB两种协议，JSONPath是一等公民，支持全量解析和部分解析，支持Java服务端、客户端Android、大数据场景。

* JSONB格式文档 https://github.com/alibaba/fastjson2/wiki/jsonb_format_cn
* FASTJSON 2性能有了很大提升，具体性能数据看这里 https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark

# 2. 使用前准备
## 2.1 Maven依赖
在fastjson 2.0中，groupId和1.x不一样，是com.alibaba.fastjson2
```xml
<dependency>
	<groupId>com.alibaba.fastjson2</groupId>
	<artifactId>fastjson2</artifactId>
	<version>2.0.1</version>
</dependency>
```
https://repo1.maven.org/maven2/com/alibaba/fastjson2/fastjson2/


## 2.2
如果原来使用fastjson 1.2.x版本，可以使用兼容包，兼容包不能保证100%兼容，请仔细测试验证，发现问题请及时反馈。
```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>fastjson</artifactId>
	<version>2.0.1</version>
</dependency>
```

## 2.2 常用类和方法
在fastjson 2.0中，package和1.x不一样，是com.alibaba.fastjson2。如果你之前用的是fastjson1，大多数情况直接更包名就即可。
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

# 3. 读取JSON对象
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

# 4. 将JavaBean对象生成JSON
## 4.1 将JavaBean对象生成JSON格式的字符串
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
	"id"	: 1001,
	"name"	: "DataWorks"
}

JSON.toJSONString(product, JSONWriter.Feature.BeanToArray);
// 生成如下的结果
[123, "DataWorks"]
```

## 4.2 将JavaBean对象生成UTF8编码的byte[]
```java
Product product = ...;
byte[] utf8JSONBytes = JSON.toJSONBytes(product);
```

## 4.3 将JavaBean对象生成JSONB格式的byte[]
```java
Product product = ...;
byte[] jsonbBytes = JSONB.toBytes(product);

byte[] jsonbBytes = JSONB.toBytes(product, JSONWriter.Feature.BeanToArray);
```

# 5. 读取JavaBean
## 5.1 将字符串读取成JavaBean
```java
String str = "{\"id\":123}";
Product product = JSON.parseObject(str, Product.class);
```

## 5.2 将UTF8编码的byte[]读取成JavaBean
```java
byte[] utf8Bytes = "{\"id\":123}".getBytes(StandardCharsets.UTF_8);
Product product = JSON.parseObject(utf8Bytes, Product.class);
```

## 5.3 将JSONB数据读取成JavaBean
```java
byte[] jsonbBytes = ...
Product product = JSONB.parseObject(jsonbBytes, Product.class);

Product product = JSONB.parseObject(jsonbBytes, Product.class, JSONReader.Feature.SupportBeanArrayMapping);
```

# 6. 使用JSONPath
## 6.1 使用JSONPath部分读取数据
```java
String str = ...;

JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.of(str);
Object result = path.extract(parser);
```

## 6.2 使用JSONPath读取部分utf8Bytes的数据
```java
byte[] utf8Bytes = ...;

JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.of(utf8Bytes);
Object result = path.extract(parser);
```

## 6.3 使用JSONPath读取部分jsonbBytes的数据
```java
byte[] jsonbBytes = ...;

JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.ofJSONB(jsonbBytes); // 注意，这是利用ofJSONB方法
Object result = path.extract(parser);
```








