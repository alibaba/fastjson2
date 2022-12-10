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

`FASTJSON v2` is an upgrade of the `FASTJSON`, with the goal of providing a highly optimized `JSON` librarray for the next ten years.

- Supports the JSON and JSONB Protocols.
- Supports full parsing and partial parsing.
- Supports Java servers and Android Clients, and has big data applications.
- Supports Kotlin [https://alibaba.github.io/fastjson2/kotlin_en](https://alibaba.github.io/fastjson2/kotlin_en)
- Supports Android 8+ [(2.0.21.android)](https://repo1.maven.org/maven2/com/alibaba/fastjson2/fastjson2/2.0.21.android/)
- Supports `Graal Native-Image` [(2.0.21.graal)](https://repo1.maven.org/maven2/com/alibaba/fastjson2/fastjson2/2.0.21.graal/)
- Supports `JSON Schema` [https://alibaba.github.io/fastjson2/json_schema_cn](https://alibaba.github.io/fastjson2/json_schema_cn)

![fastjson](docs/logo.jpg "fastjson")

Related Documents:

- `JSONB` format documentation:  
  [https://alibaba.github.io/fastjson2/jsonb_format_cn](https://alibaba.github.io/fastjson2/jsonb_format_cn)
- `FASTJSON v2`'s performance has been significantly improved. For the benchmark, see here:  
  [https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark](https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark)

# 1. Prepare

## 1.1 Download

`FASTJSONv2`'s groupId is different from versions `1.x`, it is instead `com.alibaba.fastjson2`:

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.21</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2:2.0.21'
}
```

Find the latest version of `FASTJSONv2` at [maven.org](https://search.maven.org/artifact/com.alibaba.fastjson2/fastjson2).

## 1.2 Other modules

### Compatible dependence of fastjson-v1

If you are using `fastjson 1.2.x`, you can use the compatibility package. The compatibility package cannot guarantee 100% compatibility. Please test  it yourself and report any problems.

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.21</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba:fastjson:2.0.21'
}
```

### `Kotlin` integration module `fastjson-kotlin`

If your project uses `kotlin`, you can use the `Fastjson-Kotlin` module, and use the characteristics of `kotlin`.

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-kotlin</artifactId>
    <version>2.0.21</version>
</dependency>
```

`Kotlin Gradle`:

```kotlin
dependencies {
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.21")
}
```

### `Extension` integration module `fastjson-extension`

If your project uses a framework such as `SpringFramework`, you can use the `fastjson-extension` module, please refer to the usage [SpringFramework Support](docs/spring_support_en.md).

`Maven`:

```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension</artifactId>
    <version>2.0.21</version>
</dependency>
```

`Gradle`:

```groovy
dependencies {
    implementation 'com.alibaba.fastjson2:fastjson2-extension:2.0.21'
}
```

# 2. Usage

The package name of `fastjson v2` is different from `fastjson v1`. It is `com.alibaba.fastjson2`. If you used `fastjson v1` before, simply change the package name.

### 2.1 Parse `JSON` into `JSONObject`

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

### 2.2 Parse `JSON` into `JSONArray`

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

### 2.3 Parse `JSON` into a Java Object

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

### 2.4 Serialization Java Object to `JSON`

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

### 2.5 Use `JSONObject`, `JSONArray`

#### 2.5.1 Get simple property

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

#### 2.5.2 Get JavaBean

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

#### 2.5.3 Convert to JavaBean

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

### 2.6 Serialize `JavaBean` to `JSON`

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

Serialization result:
```json
{
    "id"   : 2,
    "name" : "FastJson2"
}
```

# 3. Advanced usage

### 3.1 Use `JSONB`

#### 3.1.1 Serialize `JavaBean` to `JSONB`

```java
User user = ...;
byte[] bytes = JSONB.toBytes(user);
byte[] bytes = JSONB.toBytes(user, JSONWriter.Feature.BeanToArray);
```

#### 3.1.2 Parse `JSONB` to `JavaBean`

```java
byte[] bytes = ...
User user = JSONB.parseObject(bytes, User.class);
User user = JSONB.parseObject(bytes, User.class, JSONReader.Feature.SupportBeanArrayMapping);
```

### 3.2 Use `JSONPath`

#### 3.2.1 Use `JSONPath` to read partial data

```java
String text = ...;
JSONPath path = JSONPath.of("$.id"); // Cached for reuse

JSONReader parser = JSONReader.of(text);
Object result = path.extract(parser);
```

#### 3.2.2 Read part of `byte[]` data using `JSONPath`

```java
byte[] bytes = ...;
JSONPath path = JSONPath.of("$.id"); // Cached for reuse

JSONReader parser = JSONReader.of(bytes);
Object result = path.extract(parser);
```

#### 3.2.3 Read part of `byte[]` data using `JSONPath`

```java
byte[] bytes = ...;
JSONPath path = JSONPath.of("$.id"); // Cached for reuse

JSONReader parser = JSONReader.ofJSONB(bytes); // Use ofJSONB method
Object result = path.extract(parser);
```
