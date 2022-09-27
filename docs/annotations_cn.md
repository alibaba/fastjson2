# FASTJSON2提供的Annotations

## 1. JSONField
JSONField是作用在Field、Method、Parameter上的Annotation，可以用来指定序列化字段的顺序、名字、格式、是否忽略、配置JSONReader/JSONWriter的Features等。

### 1.1 定制名字序列化和反序列化
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

### 1.3 忽略字段
可以通过JSONField.serialize配置该字段是否要序列化，通过JSONField.deserialize配置该字段是否需要反序列化。

* 配置序列化反序列化忽略特定字段
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

## 2. JSONType
JSONType是配置在Class/Interface上的Annotation，可以配置改类型的所有字段的NamingStrategy、序列化和反序列化忽略的字段、JSONReader/JSONWriter的Features等。

### 2.1 配置序列化和反序列化忽略的字段
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

# 3. 基于mixin机制注入Annotation
当你需要定制化序列化一些LIB里面的类，你无法修改这些类的代码，你可以使用FASTJSON2的Minxin功能注入Annotation。
具体使用介绍参考这里： [https://alibaba.github.io/fastjson2/mixin_cn](https://alibaba.github.io/fastjson2/mixin_cn)
