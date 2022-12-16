# 1. Prepare

If your project uses `kotlin`, you can use the` Fastjson-Kotlin` module, and use the characteristics of `kotlin`.

### 1.1 Download

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

### 1.2 Import functions

Whenever the function in the `Fastjson-Kotlin`, you need to complete the import functions work, **otherwise it will be prompted that the corresponding function will not be found**.

E.g:

```kotlin
import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.into
```

If you use a lot of functions, you can use batch import.

```kotlin
import com.alibaba.fastjson2.*
```

# 2. Usage

We have unified function names `to` and` into`.

- Use `to` to use `::class.java`, suitable for categories without generic Class.
- Use `into` to use the `TypeReference`, which is suitable for genetic Class.

First define a User class

```kotlin
class User(
    var id: Int,
    var name: String
)
```

### 2.1 Parse `JSON` into `JSONObject`

```kotlin
val text = "..." // String
val data = text.parseObject()

val bytes = ... // ByteArray
val data = bytes.parseObject() // JSONObject
```

### 2.2 Parse `JSON` into `JSONArray`

`Kotlin`:

```kotlin
val text = "..." // String
val data = text.parseArray() // JSONArray
```

### 2.2 Create the `Typereference` of specified Class

```kotlin
val refer = reference<User>()
```

### 2.3 Parse `JSON` into an Object

No generic:

```kotlin
val text = "..." // String
val data = text.to<User>() // User
```

Including generic:

```kotlin
val text = "..." // String
val data = text.into<List<User>>() // List<User>
val data = text.into<Map<String, User>>() // Map<String, User>
```

### 2.4 Serialization Object to `JSON`

Serialization as a string:

```kotlin
val data = "..." // Any
val text = text.toJSONString() // String
```

Serialization as a ByteArray:

```kotlin
val data = "..." // Any
val bytes = text.toJSONByteArray() // ByteArray
```

### 2.5 Use `JSONObject`、`JSONArray`

#### 2.5.1 Get simple property

```kotlin
val text = "..."
val data = JSON.parseObject(text) // JSONObject

val id = data.getIntValue("id") // Int
val name = data.getString("name") // String
```

#### 2.5.2 Get Bean Object

No generic:

```kotlin
val obj = ... // JSONObject
val array = ... // JSONArray

val user = array.to<User>(0)
val user = obj.to<User>("key")
```

Including generic:

```kotlin
val obj = ... // JSONObject
val array = ... // JSONArray

val user = array.into<List<User>>(0)
val user = obj.into<List<User>>("key")
```

#### 2.5.3 Convert to Bean Object

No generic:

```kotlin
val obj = ... // JSONObject
val array = ... // JSONArray

val user = obj.to<User>() // User
val users = array.toList<User>() // List<User>
```

Including generic:

```kotlin
val obj = ... // JSONObject
val array = ... // JSONArray

val user = obj.into<HashMap<String, User>>() // HashMap<String, User>
val users = array.into<ArrayList<User>>() // ArrayList<User>
```

### 2.5 Convert `URL`、`InputStream` to Bean Object

No generic:

```kotlin
val url = ... // URL
val data = url.to<User>()
```

```kotlin
val input = ... // InputStream
val data = input.to<User>()
```

Including generic:

```kotlin
val url = ... // URL
val data = url.into<List<User>>()
```

```kotlin
val input = ... // InputStream
val data = input.into<List<User>>()
```

# 3. Advanced usage

### 3.1 Use `JSONPath`

#### 3.1.1 Use `JSONPath` to read specified data

```kotlin
val text = "..."
val path = "$.id".toPath() // JSONPath

val parser = JSONReader.of(text)
val result = path.extract(parser)
```
