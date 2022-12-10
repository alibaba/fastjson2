# 1. 使用准备

如果项目使用`Kotlin`，可以使用`fastjson-kotlin`模块，使用方式上采用`kotlin`的特性。

### 1.1 添加依赖

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

### 1.2 导包工作

每当调用`fastjson-kotlin`里的函数时，需要完成导包工作，**否则会提示找不到相应函数**。

例如:

```kotlin
import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.into
```

如果使用的函数很多时，可以使用通配符导入。

```kotlin
import com.alibaba.fastjson2.*
```

# 2. 简单使用

这里，我们对函数名为`to`和`into`做了统一。

- 使用`to`时利用`::Class.java`，适用于不含泛型的类。
- 使用`into`时利用`TypeReference`，适用于含泛型的类。

首先实例定义一个公共类

```kotlin
class User(
    var id: Int,
    var name: String
)
```

### 2.1 将`JSON`解析为`JSONObject`

```kotlin
val text = "..." // String
val data = text.parseObject()

val bytes = ... // ByteArray
val data = bytes.parseObject() // JSONObject
```

### 2.2 将`JSON`解析为`JSONArray`

`Kotlin`:

```kotlin
val text = "..." // String
val data = text.parseArray() // JSONArray
```

### 2.2 实例指定类的`TypeReference`

```kotlin
val refer = reference<User>()
```

### 2.3 将`JSON`解析为实例对象

无泛型实例:

```kotlin
val text = "..." // String
val data = text.to<User>() // User
```

含泛型实例:

```kotlin
val text = "..." // String
val data = text.into<List<User>>() // List<User>
val data = text.into<Map<String, User>>() // Map<String, User>
```

### 2.4 将实例对象序列化为`JSON`

序列化为字符串:

```kotlin
val data = "..." // Any
val text = text.toJSONString() // String
```

序列化为字节数组:

```kotlin
val data = "..." // Any
val bytes = text.toJSONByteArray() // ByteArray
```

### 2.5 使用`JSONObject`、`JSONArray`

#### 2.5.1 获取简单属性

```kotlin
val text = "..."
val data = JSON.parseObject(text) // JSONObject

val id = data.getIntValue("id") // Int
val name = data.getString("name") // String
```

#### 2.5.2 读取实例对象

无泛型实例:

```kotlin
val obj = ... // JSONObject
val array = ... // JSONArray

val user = array.to<User>(0)
val user = obj.to<User>("key")
```

含泛型实例:

```kotlin
val obj = ... // JSONObject
val array = ... // JSONArray

val user = array.into<List<User>>(0)
val user = obj.into<List<User>>("key")
```

#### 2.5.3 转为实例对象

无泛型实例:

```kotlin
val obj = ... // JSONObject
val array = ... // JSONArray

val user = obj.to<User>() // User
val users = array.toList<User>() // List<User>
```

含泛型实例:

```kotlin
val obj = ... // JSONObject
val array = ... // JSONArray

val user = obj.into<HashMap<String, User>>() // HashMap<String, User>
val users = array.into<ArrayList<User>>() // ArrayList<User>
```

### 2.5 `URL`、`InputStream`转为实例对象

无泛型实例:

```kotlin
val url = ... // URL
val data = url.to<User>()
```

```kotlin
val input = ... // InputStream
val data = input.to<User>()
```

含泛型实例:

```kotlin
val url = ... // URL
val data = url.into<List<User>>()
```

```kotlin
val input = ... // InputStream
val data = input.into<List<User>>()
```

# 3. 进阶使用

### 3.1 使用`JSONPath`

#### 3.1.1 使用`JSONPath`读取部分数据

```kotlin
val text = "..."
val path = "$.id".toPath() // JSONPath

val parser = JSONReader.of(text)
val result = path.extract(parser)
```
