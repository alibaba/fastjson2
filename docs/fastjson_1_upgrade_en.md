# FASTJSON 1.x Upgrade Guide

## 1. Why Upgrade?
*   **Better Performance**: For detailed performance data, see https://github.com/alibaba/fastjson2/wiki/fastjson_benchmark_en
*   **Support for New JDK Features**: Includes support for Records introduced in JDK 14, more native support for Lambda expressions, and GraalVM Native-Image support.
*   **Native Kotlin Support**
*   **Support for JSON Schema**: https://github.com/alibaba/fastjson2/wiki/json_schema_en
*   **Unified Text and Binary API**: FASTJSON v2 can also be used in RPC and Redis scenarios.
*   **More Secure**: The autoType whitelist has been completely removed to enhance security. https://github.com/alibaba/fastjson2/wiki/fastjson2_autotype_en
*   **Long-Term Maintenance**: The new version will be maintained for the long term, aiming to provide a high-performance JSON library for the future. Feature requests will get faster responses, and bugs will be fixed more quickly.

## 2. How to Upgrade

### 2.1. How to Get the Latest Version
The FASTJSON v2 project is currently active, and new versions are released from time to time. You can get the latest version from the fastjson2 release page: [https://github.com/alibaba/fastjson2/releases](https://github.com/alibaba/fastjson2/releases)

You can upgrade in two modes:
*   Compatibility Mode
*   Using the new fastjson v2 API

### 2.2. Upgrading in Compatibility Mode
You can upgrade using compatibility mode, which does not require code changes. However, in scenarios of deep usage, it may not be fully compatible. Although this mode is convenient, please test thoroughly and report any issues to [https://github.com/alibaba/fastjson2/issues](https://github.com/alibaba/fastjson2/issues).

*   **Compatibility Mode Maven Dependency**
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
```

### 2.3. Upgrading Using the New API
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

If you need Spring support, you also need to depend on `fastjson2-extension`:
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-extension</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
```

If you are using fastjson in Kotlin, you need to depend on `fastjson2-kotlin`:
```xml
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2-kotlin</artifactId>
    <version>${fastjson2.version}</version>
</dependency>
```

## 3. Common Issues
### 3.1. How to make `ParserConfig.getGlobalInstance().addAccept()` compatible?
In v2, the functionality to add an autoType whitelist to `ParserConfig` is provided by `ObjectReaderProvider`. You can configure the autoType whitelist as follows:
```java
JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("com.mycompany.xxx");
```

### 3.2. `ObjectSerializer` and `ObjectDeserializer` have been removed. What are the new alternatives?
FASTJSON v2 has a more comprehensive extension mechanism, as follows:
*   Annotation Introduction: [https://alibaba.github.io/fastjson2/annotations_en](https://alibaba.github.io/fastjson2/annotations_en)
*   Annotation Mixin Introduction: [https://alibaba.github.io/fastjson2/mixin_en](https://alibaba.github.io/fastjson2/mixin_en)
*   Feature Introduction: [https://alibaba.github.io/fastjson2/features_en](https://alibaba.github.io/fastjson2/features_en)
*   Using Mixin to customize serialization and deserialization: [https://alibaba.github.io/fastjson2/mixin_en](https://alibaba.github.io/fastjson2/mixin_en)
*   Implementing `ObjectWriter` and `ObjectReader` for custom serialization and deserialization: [https://alibaba.github.io/fastjson2/register_custom_reader_writer_en](https://alibaba.github.io/fastjson2/register_custom_reader_writer_en)

### 3.3. Common Class Extension Upgrade Mapping
| fastjson1                                                   | fastjson2                                             |
|-------------------------------------------------------------|-------------------------------------------------------|
| com.alibaba.fastjson.parser.ParserConfig                    | com.alibaba.fastjson2.reader.ObjectReaderProvider     |
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
| com.alibaba.fastjson.serializer.ToStringSerializer          | com.alibaba.fastjson2.writer.ObjectWriterImplToString |
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
