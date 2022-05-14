[![Java CI](https://img.shields.io/github/workflow/status/alibaba/fastjson2/Java%20CI/main?logo=github&logoColor=white)](https://github.com/alibaba/fastjson2/actions/workflows/ci.yaml)
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

##### Language: [中文](README.md) | English

# FASTJSON v2

`FASTJSONv2` is an upgrade of the `FASTJSON`, with the goal of providing a highly optimized `JSON` library for the next ten years.

- Supports the JSON and JSONB Protocols.
- Supports full parsing and partial parsing.
- Supports Java servers and Android Clients, and has big data applications.
- Supports Kotlin

![fastjson](docs/logo.jpg "fastjson")

Related Documents:

- `JSONB` format documentation:  
  https://github.com/alibaba/fastjson2/wiki/jsonb_format_cn
- `FASTJSON v2`'s performance has been significantly improved. For the benchmark, see here:  
  https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark

# 1. Prepare

## 1.1 Download

`FASTJSONv2`'s groupId is different from versions `1.x`, it is instead `com.alibaba.fastjson2`:

Maven:
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.3</version>
</dependency>
```

Gradle:
```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2:2.0.3'
}
```

Find the latest version of `FASTJSONv2` at [maven.org](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2).

## 1.2 Compatible

### Compatible dependence of fastjson-v1

If you are using `fastjson 1.2.x`, you can use the compatibility package. The compatibility package cannot guarantee 100% compatibility. Please test  it yourself and report any problems.

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.3</version>
</dependency>
```

Gradle:
```groovy
dependencies {
    implementation 'com.alibaba:fastjson:2.0.3'
}
```

### Compatible dependence of fastjson-kotlin

If your project uses `kotlin`, you can use the` Fastjson-Kotlin` module, and use the characteristics of `kotlin`.

Maven:
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-kotlin</artifactId>
    <version>2.0.3</version>
</dependency>
```

Kotlin Gradle:
```kotlin
dependencies {
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.3")
}
```

# 2 Usage

The package name of `fastjson v2` is different from `fastjson v1`. It is `com.alibaba.fastjson2`. If you used `fastjson v1` before, simply change the package name.

### 2.1 Parse `JSON` into `JSONObject`

Java:
```java
String text = "...";
JSONObject data = JSONObject.parseObject(text);

byte[] bytes = ...;
JSONObject data = JSONObject.parseObject(bytes);
```

Kotlin:
```kotlin
import com.alibaba.fastjson2.*

val text = ... // String
val data = text.parseObject()

val bytes = ... // ByteArray
val data = bytes.parseObject() // JSONObject
```

### 2.2 Parse `JSON` into `JSONArray`

Java:
```java
String text = "...";
JSONArray data = JSONObject.parseArray(text);
```

Kotlin:
```kotlin
import com.alibaba.fastjson2.*

val text = ... // String
val data = text.parseArray() // JSONArray
```

### 2.3 Parse `JSON` into a Java Object

Java:
```java
String text = "...";
User data = JSONObject.parseObject(text, User.class);
```

Kotlin:
```kotlin
import com.alibaba.fastjson2.*

val text = ... // String
val data = text.to<User>() // User
val data = text.parseObject<User>() // User
```

### 2.4 Serialization Java Object to `JSON`

Java:
```java
Object data = "...";
String text = JSONObject.toJSONString(data);
byte[] text = JSONObject.toJSONBytes(data);
```

Kotlin:
```kotlin
import com.alibaba.fastjson2.*

val data = ... // Any
val text = text.toJSONString() // String
val bytes = text.toJSONByteArray() // ByteArray
```

### 2.2 Other

```java
package com.alibaba.fastjson2;

class JSONB {
    // Parse a JSONB byte array into a Java Object
    static T parseObject(byte[] jsonbBytes, Class<T> objectClass);

    // Convert a Java Object into a JSONB Byte Array
    static byte[] toBytes(Object object);
}

class JSONObject {
    Object get(String key);
    int getIntValue(String key);
    Integer getInteger(String key);
    long getLongValue(String key);
    Long getLong(String key);
    T getObject(String key, Class<T> objectClass);

    // Convert JSONObject into a Java Object
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
    // Construct a JSONPath
    static JSONPath of(String path);

    // The input is directly parsed according to the path,
    // which will be parsed and optimized but not fully parsed.
    Object extract(JSONReader jsonReader);

    // Evaluate object based on the path
    Object eval(Object rootObject);
}

class JSONReader {
    // Constructs a JSONReader given a JSON String
    static JSONReader of(String str);

    // Constructs a JSONReader given a UTF-8 encoded byte array
    static JSONReader of(byte[] utf8Bytes);

    // Construct a JSONReader given a char array
    static JSONReader of(char[] chars);

    // Construct a JSONReader given a JSONB-formatted byte array
    static JSONReader ofJSONB(byte[] jsonbBytes);
}
```

# 3. Reading a `JSON` Object

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

# 4. Generate `JSON` using a `JavaBean` object

## 4.1 Generating a `JSON` String using `JavaBean`

```java
class Product {
    public int id;
    public String name;
}

Product product = new Product();
product.id = 1001;
product.name = "DataWorks";

JSON.toJSONString(product);

// Produces the following result
{
    "id"   : 1001,
    "name" : "DataWorks"
}

JSON.toJSONString(product, JSONWriter.Feature.BeanToArray);
// Produces the following result
[1001, "DataWorks"]
```

## 4.2 Generating a UTF-8 encoded byte array from a ``JavaBean`` Object

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

# 5. Reading `JSON` using `JavaBean`

## 5.1 Reading a String using `JavaBean`

```java
String str = "{\"id\":123}";
Product product = JSON.parseObject(str, Product.class);
```

## 5.2 Reading a `UTF-8`encoded byte array with `JavaBean`

```java
byte[] utf8Bytes = "{\"id\":123}".getBytes(StandardCharsets.UTF_8);
Product product = JSON.parseObject(utf8Bytes, Product.class);
``` 

## 5.3 Reading `JSONB` data with `JavaBean`

```java
byte[] jsonbBytes = ...
Product product = JSONB.parseObject(jsonbBytes, Product.class);

Product product = JSONB.parseObject(jsonbBytes, Product.class, JSONReader.Feature.SupportBeanArrayMapping);
```

# 6. Using `JSONPath`

## 6.1 Use `JSONPath` selection to read data

```java
String str = ...;

// Caching and Reusing can improve performance
JSONPath path = JSONPath.of("$.id"); 

JSONReader parser = JSONReader.of(str);
Object result = path.extract(parser);
```

## 6.2 Reading partial `utf8Bytes` data using `JSONPath`

```java
byte[] utf8Bytes = ...;

// Caching and Reusing can improve perforamance
JSONPath path = JSONPath.of("$.id"); 

JSONReader parser = JSONReader.of(utf8Bytes);
Object result = path.extract(parser);
```

## 6.3 Reading partial `jsonbBytes` data using `JSONPath`

```java
byte[] jsonbBytes = ...;

// Caching and Reusing can improve performance
JSONPath path = JSONPath.of("$.id");

// Note that this is using the ofJSONB method
JSONReader parser = JSONReader.ofJSONB(jsonbBytes); 
Object result = path.extract(parser);
```
