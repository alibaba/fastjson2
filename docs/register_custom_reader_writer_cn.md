# 实现ObjectWriter和ObjectReader实现定制序列化和反序列化

## 1. 实现ObjectWriter接口定制化序列化

## 1.1 ObjectWriter的定义
```java
package com.alibaba.fastjson2.writer;

public interface ObjectWriter<T> {
    void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features);
}
```

## 1.2 实现ObjectWriter举例
```java
class InetAddressWriter implements ObjectWriter {
    InetAddressWriter INSTANCE = new InetAddressWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        InetAddress address = (InetAddress) object;
        // 优先使用name
        jsonWriter.writeString(address.getHostName());
    }
}
```

## 1.3 注册Writer
```java
JSON.register(InetAddress.class, InetAddressWriter.INSTANCE);
JSON.register(Inet4Address.class, InetAddressWriter.INSTANCE);
JSON.register(Inet6Address.class, InetAddressWriter.INSTANCE);
```

## 1.4 注册ObjectWriters的一些内置实现
ObjectWriters提供了构造ofToString/ofToInt/ofToLong方法，让使用者能更方便序列化，比如：

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

# 2. 实现ObjectReader定制反序列化

## 2.1 ObjectReader的定义
```java
package com.alibaba.fastjson2.reader;

public interface ObjectReader<T> {
    T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features);
}
```
ObjectReader还有很多其他方法，但都提供了缺省实现，上面这个方法是必须实现的。

## 2.2 实现ObjectReader举例
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

## 2.3 注册Writer
```java
JSON.register(Instant.class, ObjectReader.INSTANCE);
```

## 2.4 注册内置的ObjectReaders提供的一些内置实现
ObjectReaders提供了构造ofString/ofInt/ofLong方法，让使用者能更方便反序列化，比如：
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
