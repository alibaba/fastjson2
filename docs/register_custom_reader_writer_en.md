# Implementing ObjectWriter and ObjectReader for Custom Serialization and Deserialization

## 1. Implementing the ObjectWriter Interface for Custom Serialization

### 1.1 ObjectWriter Definition
```java
package com.alibaba.fastjson2.writer;

public interface ObjectWriter<T> {
    void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features);
}
```

### 1.2 ObjectWriter Implementation Example
```java
class InetAddressWriter implements ObjectWriter {
    static final InetAddressWriter INSTANCE = new InetAddressWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        InetAddress address = (InetAddress) object;
        // Prioritize using the name
        jsonWriter.writeString(address.getHostName());
    }
}
```

### 1.3 Registering the Writer
```java
JSON.register(InetAddress.class, InetAddressWriter.INSTANCE);
JSON.register(Inet4Address.class, InetAddressWriter.INSTANCE);
JSON.register(Inet6Address.class, InetAddressWriter.INSTANCE);
```

### 1.4 Registering Some Built-in Implementations of ObjectWriters
`ObjectWriters` provides `ofToString`/`ofToInt`/`ofToLong` methods to make serialization more convenient for users, for example:

```java
 public static class Bean {
    public int id;

    public String toString() {
        return Integer.toString(id);
    }

    public int getId() {
        return id;
    }
}

@Test
public void testToString() {
    ObjectWriter objectWriter = ObjectWriters.ofToString(Bean::toString);

    Bean bean = new Bean();
    bean.id = 101;

    JSONWriter jsonWriter = JSONWriter.of();
    objectWriter.write(jsonWriter, bean);
    assertEquals("\"101\"", jsonWriter.toString());
}

@Test
public void testToInt() {
    ObjectWriter<Bean> objectWriter = ObjectWriters.ofToInt(
            (ToIntFunction<Bean>) Bean::getId
    );

    Bean bean = new Bean();
    bean.id = 101;

    JSONWriter jsonWriter = JSONWriter.of();
    objectWriter.write(jsonWriter, bean);
    assertEquals("101", jsonWriter.toString());
}
```

# 2. Implementing ObjectReader for Custom Deserialization

## 2.1 ObjectReader Definition
```java
package com.alibaba.fastjson2.reader;

public interface ObjectReader<T> {
    T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features);
}
```
`ObjectReader` has many other methods, but they all provide default implementations. The method above must be implemented.

## 2.2 ObjectReader Implementation Example
```java
import java.time.Instant;
import com.alibaba.fastjson2.reader.ObjectReader;

class InstantReader implements ObjectReader {
    public static final InstantReader INSTANCE = new InstantReader();
    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        long millis = jsonReader.readInt64Value();
        return Instant.ofEpochMilli(millis);
    }
}
```

## 2.3 Registering the Reader
```java
JSON.register(Instant.class, InstantReader.INSTANCE);
```

## 2.4 Registering Some Built-in Implementations Provided by ObjectReaders
`ObjectReaders` provides `ofString`/`ofInt`/`ofLong` methods to make deserialization more convenient for users, for example:
```java
public static class Bean {
    final int code;
    public Bean(String str) {
        code = Integer.parseInt(str);
    }
}

@Test
public void test() {
    JSON.register(Bean.class, ObjectReaders.ofString(Bean::new));
    
    Bean bean = JSON.parseObject("\"123\"", Bean.class);
    assertEquals(123, bean.code);
}
```
