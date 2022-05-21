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
