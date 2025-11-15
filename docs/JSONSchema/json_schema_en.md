# FASTJSON v2 JSONSchema Support

Since version 2.0.4, fastjson has provided support for JSONSchema. For the specific JSON Schema specification, please refer to https://json-schema.org/

## 1. Performance of FASTJSON2 JSONSchema
As always, the performance of FASTJSON2 JSONSchema is excellent, far surpassing its competitors. As the following test results show, fastjson2's performance is 9 times that of networknt and 6 times that of everit.

```java
Benchmark                       Mode  Cnt   Score   Error   Units
JSONSchemaBenchmark.everit     thrpt    5   3.182 ± 0.018  ops/ms
JSONSchemaBenchmark.fastjson2  thrpt    5  21.408 ± 0.147  ops/ms
JSONSchemaBenchmark.networknt  thrpt    5   2.337 ± 0.007  ops/ms
```

* Test code: https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/jsonschema/JSONSchemaBenchmark.java

## 2. Direct Validation by Constructing a JSONSchema Object

```java
@Test
public void test() {
    // Define that the object must contain 'longitude' and 'latitude' properties,
    // where longitude's value range is [-180, 180] and latitude's is [-90, 90]
    JSONSchema schema = JSONSchema.of(JSON.parseObject("{" +
        "  \"type\": \"object\"," +
        "  \"properties\": {" +
        "    \"longitude\": { \"type\": \"number\", \"minimum\":-180, \"maximum\":180}," +
        "    \"latitude\": { \"type\": \"number\", \"minimum\":-90, \"maximum\":90}," +
        "  }," +
        "  \"required\": [\"longitude\", \"latitude\"]" +
        "}"));

    // Validate a JSONObject, validation passes
    assertTrue(
            schema.isValid(
                    JSONObject.of("longitude", 120.1552, "latitude", 30.2741)
            )
    );

    // Validate a JSONObject, fails because longitude exceeds the maximum value
    assertFalse(
            schema.isValid(
                    JSONObject.of("longitude", 220.1552, "latitude", 30.2741)
            )
    );

    // Validate a JavaBean object, validation passes
    assertTrue(
            schema.isValid(
                    new Point(120.1552, 30.2741)
            )
    );

    // Validate a JavaBean object, fails because latitude exceeds the maximum value
    assertFalse(
            schema.isValid(
                    new Point(120.1552, 130.2741)
            )
    );
}

public static class Point {
    public final double longitude;
    public final double latitude;

    public Point(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
```

## 3. Configuring schema on JSONField
Since version 2.0.4, you can configure a schema on the `JSONField` annotation to validate the input JSON data.

```java
public static class Point1 {
    @JSONField(schema = "{'minimum':-180,'maximum':180}")
    public double longitude;


    @JSONField(schema = "{'minimum':-90,'maximum':90}")
    public double latitude;
}

@Test
public void test1() {
    // parseObject validation passes
    JSON.parseObject("{\"longitude\":120.1552,\"latitude\":30.2741}", Point1.class);

    // JSONObject to JavaObject, validation passes
    JSONObject.of("longitude", 120.1552, "latitude", 30.2741)
            .to(Point1.class);

    // parseObject validation fails, longitude exceeds the maximum value
    assertThrows(JSONSchemaValidException.class, () ->
        JSON.parseObject("{\"longitude\":220.1552,\"latitude\":30.2741}", Point1.class)
    );

    // Validate JSONObject fails, longitude exceeds the maximum value
    assertThrows(JSONSchemaValidException.class, () ->
            JSONObject.of("longitude", 220.1552, "latitude", 30.2741)
                    .to(Point1.class)
    );
}
```

## 4. Configuring schema on JSONType
Since version 2.0.4, you can configure a schema on the `JSONType` annotation to validate the input JSON data.

```java
@JSONType(schema = "{'properties':{'longitude':{'type':'number','minimum':-180,'maximum':180},'latitude':{'type':'number','minimum':-90,'maximum':90}}}")
public static class Point2 {
    @JSONField(schema = "{'minimum':-180,'maximum':180}")
    public double longitude;


    @JSONField(schema = "{'minimum':-90,'maximum':90}")
    public double latitude;
}

@Test
public void test2() {
    // parseObject validation passes
    JSON.parseObject("{\"longitude\":120.1552,\"latitude\":30.2741}", Point2.class);

    // JSONObject to JavaObject, validation passes
    JSONObject.of("longitude", 120.1552, "latitude", 30.2741)
            .to(Point2.class);

    // parseObject validation fails, longitude exceeds the maximum value
    assertThrows(JSONSchemaValidException.class, () ->
            JSON.parseObject("{\"longitude\":220.1552,\"latitude\":30.2741}", Point2.class)
    );

    // Validate JSONObject fails, longitude exceeds the maximum value
    assertThrows(JSONSchemaValidException.class, () ->
            JSONObject.of("longitude", 220.1552, "latitude", 30.2741)
                    .to(Point2.class)
    );
}
```

## 5. Constructing JSONSchema from a Type
When interacting between the backend and frontend, it is often necessary to convert a Java type into a JSONSchema and return it to the client.
```java
@Test
public void test() {
    JSONSchema schema = JSONSchema.of(Bean.class);
    String string = schema.toString();
    assertEquals(
            "{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"},\"name\":{\"type\":\"string\"}},\"required\":[\"id\"]}",
            string
    );
    JSONSchema pased = JSONSchema.of(JSON.parseObject(string));
    assertTrue(Differ.diff(schema, pased));

    Bean bean = new Bean();
    JSONSchema valueSchema = JSONSchema.ofValue(bean);
    assertTrue(Differ.diff(schema, valueSchema));
}

public static class Bean {
    public int id;
    public String name;
}
```

## 6. Constructing JSONSchema from a Value Object
```java
@Test
public void fromValueMap() {
    Map map = new HashMap();
    map.put("id", 123);
    assertEquals("{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}}}", JSONSchema.ofValue(map).toString());
}
```
