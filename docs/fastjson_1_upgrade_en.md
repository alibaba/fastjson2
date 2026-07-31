# FASTJSON 1.x Upgrade Guide

## 1. Why Upgrade?
*   **Better Performance**: Overall performance is better than the 1.x version.
*   **Support for New JDK Features**: Includes support for Records introduced in JDK 14, more native support for Lambda expressions, and GraalVM Native-Image support.
*   **More Secure**: The autoType whitelist has been completely removed. AutoType is disabled by default, which improves security.

## 2. How to Upgrade

### 2.1. Version Notes
This repository is a trimmed fork based on fastjson2 2.0.63. Only the `core` module is kept, providing core JSON serialization and deserialization. It does not include the 1.x compatibility package or other extension modules.

The recommended upgrade path is to use the new fastjson v2 API.

### 2.2. Upgrading Using the New API
Using the new API is the recommended upgrade method, as it provides access to more features.

*   **Package Name Change**
    FASTJSON v2 and v1.x use different package names. The new package name is `com.alibaba.fastjson2`. Because the new package is different, v1.x and v2.x can coexist.

```java
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
```

*   **Maven Dependency**
    The `groupId` for the Maven dependency is different from v1.x; it uses the new `groupId` `com.alibaba.fastjson2`.
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
```

*   **Behavioral Differences**
    *   AutoType is disabled by default. fastjson 1.x shipped with a whitelist; fastjson 2 has no whitelist at all and must be explicitly enabled.
    *   All Features are off by default. fastjson 1.x enabled several features by default.
    *   Circular reference detection is off by default. fastjson 1.x enabled it by default.
    *   SmartMatch is off by default. fastjson 1.x enabled it by default.
    *   Method names have changed. For example, `JSON.parse(String)` returns `Object`, while the common v1 parsing entry point now maps to `JSON.parseObject(String, Class)` in v2. The serialization entry point is still `JSON.toJSONString(Object)`.

## 3. Common Issues
### 3.1. How to replace the v1 autoType whitelist configuration?
In v1, the autoType whitelist was configured through a global configuration. In v2, the corresponding functionality is provided by `ObjectReaderProvider`. You can configure the autoType whitelist as follows:
```java
JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("com.mycompany.xxx");
```

### 3.2. `ObjectSerializer` and `ObjectDeserializer` have been removed. What are the new alternatives?
FASTJSON v2 has a more comprehensive extension mechanism, as follows:
*   Annotation Introduction: [annotations_en.md](annotations_en.md)
*   Feature Introduction: [features_en.md](features_en.md)
*   Using Mixin to inject Annotations for custom serialization and deserialization: [mixin_en.md](mixin_en.md)
*   Implementing `ObjectWriter` and `ObjectReader` for custom serialization and deserialization: [register_custom_reader_writer_en.md](register_custom_reader_writer_en.md)

### 3.3. Common Class Extension Upgrade Mapping
| fastjson1                                                   | fastjson2                                             |
|-------------------------------------------------------------|-------------------------------------------------------|
| com.alibaba.fastjson.parser.deserializer.ExtraProcessor     | com.alibaba.fastjson2.filter.ExtraProcessor           |
| com.alibaba.fastjson.parser.deserializer.ObjectDeserializer | com.alibaba.fastjson2.reader.ObjectReader             |
| com.alibaba.fastjson.serializer.AfterFilter                 | com.alibaba.fastjson2.filter.AfterFilter              |
| com.alibaba.fastjson.serializer.BeforeFilter                | com.alibaba.fastjson2.filter.BeforeFilter             |
| com.alibaba.fastjson.serializer.ContextValueFilter          | com.alibaba.fastjson2.filter.ContextValueFilter       |
| com.alibaba.fastjson.serializer.LabelFilter                 | com.alibaba.fastjson2.filter.LabelFilter              |
| com.alibaba.fastjson.serializer.NameFilter                  | com.alibaba.fastjson2.filter.NameFilter               |
| com.alibaba.fastjson.serializer.PascalNameFilter            | com.alibaba.fastjson2.filter.PascalNameFilter         |
| com.alibaba.fastjson.serializer.PropertyFilter              | com.alibaba.fastjson2.filter.PropertyFilter           |
| com.alibaba.fastjson.serializer.ObjectSerializer            | com.alibaba.fastjson2.writer.ObjectWriter             |
| com.alibaba.fastjson.serializer.SerializeConfig             | com.alibaba.fastjson2.writer.ObjectWriterProvider     |
| com.alibaba.fastjson.serializer.ValueFilter                 | com.alibaba.fastjson2.filter.ValueFilter              |
| com.alibaba.fastjson.serializer.SerializerFeature           | com.alibaba.fastjson2.JSONWriter.Feature              |
| com.alibaba.fastjson.parser.Feature                         | com.alibaba.fastjson2.JSONReader.Feature              |

### 3.4 Alternative for `SerializerFeature.UseISO8601DateFormat` in fastjson2

fastjson2's `JSONWriter.Feature` does not have a feature corresponding to `UseISO8601DateFormat`. The alternative is to use `format="iso8601"`, as shown below:
```java
import com.alibaba.fastjson2.JSON;

String format = "iso8601";
JSON.toJSONString(obj, format);
```

### 3.5 Alternative for `SerializerFeature.DisableCircularReferenceDetect` in fastjson2
In fastjson2, the alternative is `JSONWriter.Feature.ReferenceDetection`, but the semantics are opposite, and the default is different. `JSONWriter.Feature.ReferenceDetection` in fastjson2 is disabled by default, whereas in fastjson1 it was enabled by default.

### 3.6 Alternative for `SerializerFeature.SortField` in fastjson2
Not needed. In fastjson2, `JSONObject` inherits from `LinkedHashMap`, so this feature is not necessary.

### 3.7 Alternative for `SerializerFeature.WriteDateUseDateFormat` in fastjson2
The default behavior in fastjson2 is to use a date format. To change it to behave like fastjson 1.x (outputting milliseconds), you need to configure `format = "millis"`, as shown below:
```java
import com.alibaba.fastjson2.JSON;

String format = "millis";
JSON.toJSONString(obj, format);
```
