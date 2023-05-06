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

##### [📖 English Documentation](README_EN.md) | 📖 中文文档
##### 本项目的Issues会被同步沉淀至[阿里云开发者社区](https://developer.aliyun.com/ask/)

# FASTJSON v2

`FASTJSON 2`是一个性能极致并且简单易用的Java JSON库。

- `FASTJSON 2`是`FASTJSON`项目的重要升级，和FASTJSON 1相比，性能有非常大的提升，解决了autoType功能因为兼容和白名单的安全性问题。
- 性能极致，性能远超过其他流行JSON库，包括jackson/gson/org.json，性能数据: [https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)
- 支持JDK新特性，包括`JDK 11`/`JDK 17`，针对`compact string`优化，支持Record，支持`GraalVM Native-Image`
- 完善的[`JSONPath`](https://alibaba.github.io/fastjson2/jsonpath_cn)支持，支持[SQL:2016](https://en.wikipedia.org/wiki/SQL:2016)的JSONPath语法
- 支持`Android 8+`，客户端和服务器一套API
- 支持`Kotlin` [https://alibaba.github.io/fastjson2/kotlin_cn](https://alibaba.github.io/fastjson2/kotlin_cn)
- 支持`JSON Schema` [https://alibaba.github.io/fastjson2/json_schema_cn](https://alibaba.github.io/fastjson2/json_schema_cn)
- 新增加支持二进制格式JSONB [https://alibaba.github.io/fastjson2/jsonb_format_cn](https://alibaba.github.io/fastjson2/jsonb_format_cn)

![fastjson logo](https://user-images.githubusercontent.com/1063891/233821110-0c912009-4de3-4664-a27e-25274f2fa9c1.jpg)

# 1. 使用准备

## 1.1 添加依赖

在`fastjson v2`中，`groupId`和`1.x`不一样，是`com.alibaba.fastjson2`：

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.30</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2:2.0.30'
}
```

可以在 [maven.org](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2) 查看最新可用的版本。

## 1.2 其他模块

### `Fastjson v1`兼容模块

如果原来使用`fastjson 1.2.x`版本，可以使用兼容包，兼容包不能保证100%兼容，请仔细测试验证，发现问题请及时反馈。

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.30</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba:fastjson:2.0.30'
}
```

### `Fastjson Kotlin`集成模块

如果项目使用`Kotlin`，可以使用`fastjson-kotlin`模块，使用方式上采用`kotlin`的特性。

* `Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-kotlin</artifactId>
    <version>2.0.30</version>
</dependency>

<!-- 有些场景需要依赖kotlin-reflect -->
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
    <version>${kotlin-version}</version>
</dependency>
```

* `Kotlin Gradle`:

```kotlin
dependencies {
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.30")
}
```

### `Fastjson Extension`扩展模块

如果项目使用`SpringFramework`等框架，可以使用`fastjson-extension`模块，使用方式参考 [SpringFramework Support](docs/spring_support_cn.md)。

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension-spring5</artifactId>
    <version>2.0.30</version>
</dependency>
```

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension-spring6</artifactId>
    <version>2.0.30</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2-extension-spring5:2.0.30'
}
```


```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.30'
}
```

# 2. 简单使用

在`fastjson v2`中，`package`和`1.x`不一样，是`com.alibaba.fastjson2`。如果你之前用的是`fastjson1`，大多数情况直接更包名就即可。

### 2.1 将`JSON`解析为`JSONObject`

`Java`:

```java
String text = "...";
JSONObject data = JSON.parseObject(text);

byte[] bytes = ...;
JSONObject data = JSON.parseObject(bytes);
```

`Kotlin`:

```kotlin
import com.alibaba.fastjson2.*

val text = ... // String
val data = text.parseObject()

val bytes = ... // ByteArray
val data = bytes.parseObject() // JSONObject
```

### 2.2 将`JSON`解析为`JSONArray`

`Java`:

```java
String text = "...";
JSONArray data = JSON.parseArray(text);
```

`Kotlin`:

```kotlin
import com.alibaba.fastjson2.*

val text = ... // String
val data = text.parseArray() // JSONArray
```

### 2.3 将`JSON`解析为`Java`对象

`Java`:

```java
String text = "...";
User data = JSON.parseObject(text, User.class);
```

`Kotlin`:

```kotlin
import com.alibaba.fastjson2.*

val text = ... // String
val data = text.to<User>() // User
val data = text.parseObject<User>() // User
```

### 2.4 将`Java`对象序列化为`JSON`

`Java`:

```java
Object data = "...";
String text = JSON.toJSONString(data);
byte[] text = JSON.toJSONBytes(data);
```

`Kotlin`:

```kotlin
import com.alibaba.fastjson2.*

val data = ... // Any
val text = text.toJSONString() // String
val bytes = text.toJSONByteArray() // ByteArray
```

### 2.5 使用`JSONObject`、`JSONArray`

#### 2.5.1 获取简单属性

```java
String text = "{\"id\": 2,\"name\": \"fastjson2\"}";
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

#### 2.5.2 读取`JavaBean`

`Java`:

```java
JSONArray array = ...
JSONObject obj = ...

User user = array.getObject(0, User.class);
User user = obj.getObject("key", User.class);
```

`Kotlin`:

```kotlin
val array = ... // JSONArray
val obj = ... // JSONObject

val user = array.to<User>(0)
val user = obj.to<User>("key")
```

#### 2.5.3 转为`JavaBean`

`Java`:

```java
JSONArray array = ...
JSONObject obj = ...

User user = obj.toJavaObject(User.class);
List<User> users = array.toJavaList(User.class);
```

`Kotlin`:

```kotlin
val array = ... // JSONArray
val obj = ... // JSONObject

val user = obj.to<User>() // User
val users = array.toList<User>() // List<User>
```

### 2.6 将`JavaBean`对象序列化为`JSON`

`Java`:

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

`Kotlin`:

```kotlin
class User(
    var id: Int,
    var name: String
)

val user = User()
user.id = 2
user.name = "FastJson2"

val text = user.toJSONString() // String
val bytes = user.toJSONByteArray() // ByteArray
```

序列化结果:

```json
{
    "id"   : 2,
    "name" : "FastJson2"
}
```

# 3. 进阶使用

### 3.1 使用`JSONB`

#### 3.1.1 将`JavaBean`对象序列化`JSONB`

```java
User user = ...;
byte[] bytes = JSONB.toBytes(user);
byte[] bytes = JSONB.toBytes(user, JSONWriter.Feature.BeanToArray);
```

#### 3.1.2 将`JSONB`数据解析为`JavaBean`

```java
byte[] bytes = ...
User user = JSONB.parseObject(bytes, User.class);
User user = JSONB.parseObject(bytes, User.class, JSONReader.Feature.SupportBeanArrayMapping);
```

### 3.2 使用`JSONPath`

#### 3.2.1 使用`JSONPath`读取部分数据

```java
String text = ...;
JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.of(text);
Object result = path.extract(parser);
```

#### 3.2.2 使用`JSONPath`读取部分`byte[]`的数据

```java
byte[] bytes = ...;
JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.of(bytes);
Object result = path.extract(parser);
```

#### 3.2.3 使用`JSONPath`读取部分`byte[]`的数据

```java
byte[] bytes = ...;
JSONPath path = JSONPath.of("$.id"); // 缓存起来重复使用能提升性能

JSONReader parser = JSONReader.ofJSONB(bytes); // 注意这里使用ofJSONB方法
Object result = path.extract(parser);
```
