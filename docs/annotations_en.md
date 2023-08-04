# Annotations provided by FASTJSON2

## 1. JSONField
JSONField is an Annotation that acts on Field, Method, and Parameter. It can be used to specify the order, name, format, and whether to ignore serialized fields, configure JSONReader/JSONWriter Features, etc.

### 1.1 Attribute names when customizing serialization and deserialization

The field name of the serialized output and the mapped field name of the deserialization can be configured through `JSONField.name` .

* Configured on the public field
```java
public class A {
      @JSONField(name = "ID")
      public int id;
 }
```

* Configured on public getter/setter methods
```java
public class A {
      private int id;
 
      @JSONField(name = "ID")
      public int getId() {return id;}

      @JSONField(name = "ID")
      public void setId(int value) {this.id = id;}
}
```

### 1.2 Configure the format of field output and deserialization

For fields of Date type, it is often necessary to use a custom date format for serialization and deserialization, and the custom date format can be configured through JSONField.format.

```java
public class VO {
      // Configure date serialization and deserialization to use the yyyyMMdd date format
      @JSONField(format = "yyyyMMdd")
      public Date date;
}
```

### 1.3 Ignore fields when serializing and deserializing

You can configure whether the field needs to be serialized through JSONField.serialize, and whether the field needs to be deserialized through JSONField.deserialize.

* Ignore specific fields when configuring serialization
```java
public class VO {
      @JSONField(serialize = false)
      public Date date;
}
```

* Ignore specific fields when configuring deserialization
```java
public class VO {
      @JSONField(deserialize = false)
      public Date date;
}
```

### 1.4 The order of the serialized output of configuration fields

The order of output during serialization can be configured through JSONField.ordinal.

```java
public static class VO {
      @JSONField(ordinal = 1)
      public String type;

      @JSONField(ordinal = 2)
      public String templateId;
}
```

### 1.5 Configure Serialize Features

The serialized Feature can be specified through JSONField.serializeFeatures. More configuration features reference [https://alibaba.github.io/fastjson2/features_cn](https://alibaba.github.io/fastjson2/features_cn) 。
```java
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;
import org.junit.jupiter.api.Test;


@Test
public void test() {
    Bean bean = new Bean();
    bean.id = 100;

    assertEquals("{\"id\":\"100\"}", JSON.toJSONString(bean));
}

public static class Bean {
    @JSONField(serializeFeatures = Feature.WriteNonStringValueAsString)
    public int id;
}
```

### 1.6 Configure JavaBean serialization fields and deserialization construction methods through JSONField (value = true)

Configure @JSONField(value = true) on one of the fields. When serializing, the object will be serialized and output according to the value of the field. Configure @JSONField(value = true) on the constructor parameter, and the constructor has only one parameter, and the object will be constructed according to this parameter

For example :

```java
public static class Bean2 {
    private final int code;

    public Bean2(@JSONField(value = true) int code) {
        this.code = code;
    }

    @JSONField (value = true)
    public int getCode() {
        return code;
    }
}

@Test
public void test2() {
    Bean2 bean = new Bean2(101);
    String str = JSON.toJSONString(bean);
    assertEquals("101", str);
    Bean2 bean1 = JSON.parseObject(str, Bean2.class);
    assertEquals(bean.code, bean1.code);
}
```

### 1.6 Configure Enum to serialize and deserialize based on one of the fields through JSONField(value = true)

Configure @JSONField(value = true) on the enum field, serialization and serialization will be based on this field

For example :

```java
public enum Type {
    X(101, "Big"),
    M(102, "Medium"),
    S(103, "Small");

    private final int code;
    private final String name;

    Type(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @JSONField(value = true)
    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}

public class Bean1 {
    public Type type;
}

@Test
public void test1() {
    Bean1 bean = new Bean1();
    bean.type = Type.M;
    String str = JSON.toJSONString(bean);
    assertEquals("{\"type\":102}", str);
    Bean1 bean1 = JSON.parseObject(str, Bean1.class);
    assertEquals(bean.type, bean1.type);
}
```

## 2. @JSONType

JSONType is an Annotation configured on Class/Interface, which can configure the NamingStrategy of all fields of the modified class, fields ignored by serialization and deserialization, Features of JSONReader/JSONWriter, etc.
| JSONType Annotation Supported    | Introduction                                                        |
|---------------------|-----------------------------------------------------------|
| ignores             | Some fields are ignored when serialize                                                |
| alphabetic          | Maintain native class field order when configuring serialize                                           |
| serializeFeatures   | `Features` of `JSONWriter` when configuring serialize                          |
| deserializeFeatures | `Features` of `JSONReader` when configuring deserialize                           |
| orders              | Field order when configuring serialize                                               |
| naming              | Configure the `NamingStrategy` of the field name, for details, please refer to the `PropertyNamingStrategy` enumeration class |
| serializer          | custom serializer behavior                                                  |
| deserializer        | custom deserializer behavior                                                       |
| serializeFilters    | Control serializer behavior through custom columnization `Filters`                                   |

### 2.1 Configure ignored fields for serialize and deserialize

In the example below, the serialized output only includes id1, ignoring id2 and id3.

```java
@JSONType(ignores = {"id2", "id3"})
public static class Bean {
    public int getId() {
        return 101;
    }

    public int getId2() {
        return 102;
    }

    public int getId3() {
        return 103;
    }
}
```

### 2.2 Maintain native class field order when configuring serialize

Starting from version 2.0.13, you can use `@JSONType(alphabetic = false)` to configure serialization to maintain the order of native class fields.

```java
@Slf4j
public class JSONTypeAlphabetic {

    @JSONType(alphabetic = false)
    public static class Bean {
        public int f3;
        public int f1;
        public int f2;
        public int f0;
    }

    @Test
    public void test() {
        Bean bean = new Bean();
        bean.f0 = 101;
        bean.f1 = 102;
        bean.f2 = 103;
        bean.f3 = 104;
        log.info(JSON.toJSONString(bean));
        //{"f3":104,"f1":102,"f2":103,"f0":101}
    }
}
```

### 2.3 `Features` of `JSONReader`/`JSONWriter` when configuring serialize and deserialize

You can configure `Features` of `JSONWriter`/`JSONReader` during serialize and deserialize through `@JSONType(serializeFeatures= ...)` or `@JSONType(deserializeFeatures = ...)` annotations.

More `Features` Configuration please refer to [features_cn.md](features_cn.md) 。

```java
@Slf4j
public class JSONTypeFeatures {

    // Trim the string when serialize
    // Fields that output null when deserialize
    @JSONType(deserializeFeatures = JSONReader.Feature.TrimString, serializeFeatures = JSONWriter.Feature.WriteNulls)
    public static class OrdersBean {
        public String filed1;
        public String filed2;
    }

    @Test
    public void test() {
        OrdersBean bean = new OrdersBean();
        bean.filed1 = "fastjson2";
        
        log.info(JSON.toJSONString(bean));
        //{"filed1":"fastjson2","filed2":null}
        String json="{\"filed1\":\"   fastjson2   \",\"filed2\":\"2\"}";
        
        OrdersBean bean2 = JSON.parseObject(json, OrdersBean.class);
        log.info(bean2.filed1);
        //fastjson2
    }
}
```

### 2.4 Field order when configuring serialize

You can use `@JSONType(orders = {"filed1", "filed2"})` annotation to specify the field order during serialize.

```java
@Slf4j
public class JSONTypeOrders {

    @JSONType(orders = {"filed4", "filed3", "filed2", "filed1"})
    public static class OrdersBean {
        public String filed1;
        public String filed2;
        public String filed3;
        public String filed4;
  
    }

    @Test
    public void test() {
        OrdersBean bean = new OrdersBean();
        bean.filed1 = "1";
        bean.filed2 = "2";
        bean.filed3 = "3";
        bean.filed4 = "4";
        log.info(JSON.toJSONString(bean));
        //{"filed4":"4","filed3":"3","filed2":"2","filed1":"1"}
    }
}
```

# 3. Inject Annotation based on mixin mechanism

When you need to customize and serialize some classes in LIB, and you cannot modify the code of these classes, you can use the Minxin function of FASTJSON2 to inject Annotation.
For specific usage, refer to here: [https://alibaba.github.io/fastjson2/mixin_cn](https://alibaba.github.io/fastjson2/mixin_cn)
