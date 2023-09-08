# FASTJSON2提供的Annotations

## 1. JSONField
JSONField是作用在Field、Method、Parameter上的Annotation，可以用来指定序列化字段的顺序、名字、格式、是否忽略、配置JSONReader/JSONWriter的Features等。

### 1.1 定制序列化和反序列化时的属性名

可以通过JSONField.name来配置序列化输出的字段名和反序列化是映射的字段名。

* 配置在public field上
```java
public class A {
      @JSONField(name = "ID")
      public int id;
 }
```

* 配置在public getter/setter method上
```java
public class A {
      private int id;
 
      @JSONField(name = "ID")
      public int getId() {return id;}

      @JSONField(name = "ID")
      public void setId(int value) {this.id = id;}
}
```

### 1.2 配置字段输出和反序列化的格式
针对Date类型的字段，经常需要用定制的日期格式进行序列化和反序列化，可以通过JSONField.format来配置自定义日期格式。
```java
public class VO {
      // 配置date序列化和反序列使用yyyyMMdd日期格式
      @JSONField(format = "yyyyMMdd")
      public Date date;
}
```

### 1.3 序列化/反序列化时忽略字段
可以通过JSONField.serialize配置该字段是否要序列化，通过JSONField.deserialize配置该字段是否需要反序列化。

* 配置序列化时忽略特定字段
```java
public class VO {
      @JSONField(serialize = false)
      public Date date;
}
```

* 配置反序列化忽略特定字段
```java
public class VO {
      @JSONField(deserialize = false)
      public Date date;
}
```

### 1.4 配置字段的序列化输出的的顺序
可以通过JSONField.ordinal来配置序列化时输出的顺序。
```java
public static class VO {
      @JSONField(ordinal = 1)
      public String type;

      @JSONField(ordinal = 2)
      public String templateId;
}
```

### 1.5 配置序列化Features
可以通过JSONField.serializeFeatures来指定序列化的Feature。更多配置Features参考 [https://alibaba.github.io/fastjson2/features_cn](https://alibaba.github.io/fastjson2/features_cn) 。
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

### 1.6 通过JSONField(value = true)配置JavaBean序列化字段和反序列化构造方式
将@JSONField(value = true)配置在其中一个字段上，序列化时，会将对象序列化时按照该字段的值输出。将@JSONField(value = true)配置在构造函数参数上，而且该构造函数只有1个参数，就会按照这个参数来构造对象

比如：
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

### 1.6 通过JSONField(value = true)配置Enum基于其中一个字段序列化和反序列化
将@JSONField(value = true)配置在enum字段上，序列化和序列化都会基于这个字段来进行，比如：
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
JSONType是配置在类/接口上的注解，可以配置改类的所有字段的NamingStrategy、序列化和反序列化忽略的字段、JSONReader/JSONWriter的Features等。

| JSONType注解支持方法      | 简介                                                        |
|---------------------|-----------------------------------------------------------|
| ignores             | 序列化时忽略某些字段                                                |
| alphabetic          | 配置序列化时保持原生类字段顺序                                           |
| serializeFeatures   | 配置序列化时`JSONWriter`的`Features`                             |
| deserializeFeatures | 配置反序列化时`JSONReader`的`Features`                            |
| orders              | 配置序列化时的字段顺序                                               |
| naming              | 配置字段名的`NamingStrategy`,详细内容请参考`PropertyNamingStrategy`枚举类 |
| serializer          | 自定义序列化行为                                                  |
| deserializer        | 自定义反序列化行为                                                 |
| serializeFilters    | 通过自定义列化`Filters`控制序列化行为                                   |

### 2.1 配置序列化和反序列化时忽略某些字段

在下面的例子中，序列化输出只包括id1，忽略id2和id3。

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

### 2.2 配置序列化时保持原生类字段顺序

从2.0.13版本开始，您可以通过`@JSONType(alphabetic = false)`配置序列化时保持原生类字段顺序。

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

### 2.3 配置序列化时的`JSONReader`/`JSONWriter`的`Features`

您可以通过`@JSONType(serializeFeatures= ...)`或`@JSONType(deserializeFeatures = ...)`注解配置序列化和反序列时`JSONWriter`/`JSONReader`的`Features`。

更多`Features`配置请参考 [features_cn.md](features_cn.md) 。

```java
@Slf4j
public class JSONTypeFeatures {

    // 反序列化时对字符串进行trim
    // 序列化时输出为null的字段
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

### 2.4 配置序列化时字段顺序

您可以通过`@JSONType(orders = {"filed1", "filed2"})`注解指定序列化时的字段顺序。

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

# 3. 基于mixin机制注入Annotation
当你需要定制化序列化一些LIB里面的类，你无法修改这些类的代码，你可以使用FASTJSON2的Minxin功能注入Annotation。
具体使用介绍参考这里： [https://alibaba.github.io/fastjson2/mixin_cn](https://alibaba.github.io/fastjson2/mixin_cn)
