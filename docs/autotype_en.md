# Introduction to FASTJSON 2 AutoType Mechanism

## 1. Introduction to the AutoType Feature
FASTJSON supports the AutoType feature, which includes type information in the serialized JSON string. During deserialization, it is not necessary to pass in the type, as it enables automatic type identification.

## 2. Introduction to the AutoType Security Mechanism
*   **Must be explicitly enabled for use.** Unlike fastjson 1.x, which had a whitelist for compatibility, fastjson 2 has no whitelist at all, not even for Exception classes. It must be explicitly enabled to be used. This ensures that the default configuration is secure.
*   **Supports `safeMode` configuration.** When `safeMode` is enabled, explicitly passing AutoType parameters will have no effect.
*   **Filtered by a built-in blacklist when explicitly enabled.** This blacklist can intercept most common risks, but this mechanism cannot guarantee absolute security. Enabling AutoType should not be done in scenarios exposed to the public internet.

## 3. How to Use the AutoType Feature

### 3.1 Including Type Information During Serialization
If you need to include type information during serialization, you need to use `JSONWriter.Feature.WriteClassName`. For example:
```java
Bean bean = ...;
String jsonString = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
```

Often, the type of the root object is known, while the object fields inside are base classes or of uncertain types. In this case, not outputting the type information for the root object can reduce the size of the serialized result and improve deserialization performance.
```java
Bean bean = ...;
String jsonString = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);
```

### 3.2 Enabling the AutoType Feature for Automatic Type Support During Deserialization
```java
Bean bean = (Bean) JSON.parseObject(jsonString, Object.class, JSONReader.Feature.SupportAutoType);
```

## 4. Configuring `safeMode`
Configuring `SafeMode` will completely disable the AutoType feature. Even if the type is explicitly specified in the program, the AutoType feature will not take effect.
### 4.1 JVM Startup Parameter Configuration
```
-Dfastjson2.parser.safeMode=true
```

## 5. Using AutoTypeFilter for Automatic Types without Enabling `AutoTypeSupport`
When `AutoTypeSupport` is enabled, although there is a relatively extensive built-in blacklist, it is still not secure enough. A safer approach is to control the scope of AutoType support for the current call, avoiding enabling it globally.
```java
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {
    static final Filter autoTypeFilter = JSONReader.autoTypeFilter(
            // Add class name prefixes that need to support auto-typing as needed. The smaller the scope, the safer.
            "org.springframework.security.core.authority.SimpleGrantedAuthority"
    );

    private Class<T> clazz;

    public FastJsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONBytes(t, JSONWriter.Feature.WriteClassName);
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }

        return JSON.parseObject(bytes, clazz, autoTypeFilter);
    }
}
```
