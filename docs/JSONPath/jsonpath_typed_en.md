In big data scenarios, we often need JSONPath to return a specific type. JSONPath allows specifying a return type, which is more efficient and safer.

# 1. Interface Definition
```java
public class JSONPath {
    public static JSONPath of(String path, Type type);
}
```

# 2. Example
```java
String str = "{\"id\":1001, \"name\":\"DataWorks\"}";
JSONPath jsonPath = JSONPath.of("id", Long.class);

Long expected = 1001L;
assertEquals(expected, jsonPath.extract(str));
```
