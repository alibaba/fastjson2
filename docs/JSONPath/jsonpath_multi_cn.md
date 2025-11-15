对同一个JSON做多个JSONPath求值的场景，FASTJSON2提供了专门的API，能提升性能，如下：
```java
public class JSONPath {
    public static JSONPath of(String[] paths, Type[] types) {
        return of(paths, types, null, null);
    }
    
    public static JSONPath of(
            String[] paths,
            Type[] types,
            String[] formats,
            ZoneId zoneId,
            JSONReader.Feature... features
    );
}
```

这样构造的JSONPath，在eval或者extract方法执行时，会返回和types类型匹配的对象数组。

```java
JSONObject object = JSONObject.of("id", 1001, "name", "DataWorks", "date", "2017-07-14");

JSONPath jsonPath = JSONPath.of(
new String[]{"$.id", "$.name", "$.date"},
new Type[]{Long.class, String.class, Date.class}
);

Object[] expected = new Object[]{1001L, "DataWorks", DateUtils.parseDate("2017-07-14")};

Object[] evalResult = (Object[]) jsonPath.eval(object);
assertArrayEquals(expected, evalResult);

String jsonStr = object.toString();
Object[] result = (Object[]) jsonPath.extract(jsonStr);
assertArrayEquals(expected, result);
```
