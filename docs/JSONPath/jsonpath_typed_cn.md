在大数据场景下，我们会对JSONPath要求返回指定的类型，这个时候，JSONPath可以提供指定类型来返回，这样更高效，也更安全。

# 1. 接口定义
```java
public class JSONPath {
    public static JSONPath of(String path, Type type);
}
```

# 2. 例子
```java
String str = "{\"id\":1001, \"name\":\"DataWorks\"}";
JSONPath jsonPath = JSONPath.of("id", Long.class);

Long expected = 1001L;
assertEquals(expected, jsonPath.extract(json));
```
