For scenarios where multiple JSONPath expressions are evaluated on the same JSON, FASTJSON2 provides a specialized API that can improve performance, as shown below:
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

A `JSONPath` object constructed in this way will return an array of objects matching the specified `types` when the `eval` or `extract` methods are executed.

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
