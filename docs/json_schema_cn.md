# FASTJSON v2 JSONSchema的支持

在fastjson 2.0.4版本之后，提供了JSONSchema的支持，具体JSON Schema的规范，参考 https://json-schema.org/

## 1. FASTJSON2 JSONSchema的性能
一贯如此，FASTJSON2 JSONSchema性能非常出色，远超竞品。如下的测试表名，fastjson2性能是networknt的9倍，everit的6倍。

```java
Benchmark                       Mode  Cnt   Score   Error   Units
JSONSchemaBenchmark.everit     thrpt    5   3.182 ± 0.018  ops/ms
JSONSchemaBenchmark.fastjson2  thrpt    5  21.408 ± 0.147  ops/ms
JSONSchemaBenchmark.networknt  thrpt    5   2.337 ± 0.007  ops/ms
```

* 测试代码 https://github.com/alibaba/fastjson2/blob/main/benchmark/src/main/java/com/alibaba/fastjson2/benchmark/jsonschema/JSONSchemaBenchmark.java

## 2. 通过构造JSONSchema对象直接校验

```java
@Test
public void test() {
    // 定义必须包含longitude和latitude两个属性，其中longitude的取值范围是[-180 ~ 180]，latitude的取值范围是[-90, 90]
    JSONSchema schema = JSONSchema.of("{\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"longitude\": { \"type\": \"number\", \"minimum\":-180, \"maximum\":180},\n" +
            "    \"latitude\": { \"type\": \"number\", \"minimum\":-90, \"maximum\":90},\n" +
            "  },\n" +
            "  \"required\": [\"longitude\", \"latitude\"]\n" +
            "}");

    // 校验JSONObject对象，校验通过
    assertTrue(
            schema.isValid(
                    JSONObject.of("longitude", 120.1552, "latitude", 30.2741)
            )
    );

    // 校验JSONObject失败，longitude超过最大值
    assertFalse(
            schema.isValid(
                    JSONObject.of("longitude", 220.1552, "latitude", 30.2741)
            )
    );

    // 校验JavaBean对象，校验通过
    assertTrue(
            schema.isValid(
                    new Point(120.1552, 30.2741)
            )
    );

    // 校验JavaBean对象，校验失败，latitude超过最大值
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

## 2. 在JSONField上配置schema
2.0.4版本后，可以在Annotation JSONField上配置schema校验输入的json数据

```java
public static class Point1 {
    @JSONField(schema = "{'minimum':-180,'maximum':180}")
    public double longitude;


    @JSONField(schema = "{'minimum':-90,'maximum':90}")
    public double latitude;
}

@Test
public void test1() {
    // parseObject 校验通过
    JSON.parseObject("{\"longitude\":120.1552,\"latitude\":30.2741}", Point1.class);

    // JSONObject to JavaObject，校验通过
    JSONObject.of("longitude", 120.1552, "latitude", 30.2741)
            .to(Point1.class);

    // parseObject 校验失败，longitude超过最大值
    assertThrows(JSONSchemaValidException.class, () ->
        JSON.parseObject("{\"longitude\":220.1552,\"latitude\":30.2741}", Point1.class)
    );

    // 校验JSONObject失败，longitude超过最大值
    assertThrows(JSONSchemaValidException.class, () ->
            JSONObject.of("longitude", 220.1552, "latitude", 30.2741)
                    .to(Point1.class)
    );
}
```

## 3. 在JSONType上配置schema
2.0.4版本后，可以在Annotation JSONType上配置schema校验输入的json数据

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
    // parseObject 校验通过
    JSON.parseObject("{\"longitude\":120.1552,\"latitude\":30.2741}", Point2.class);

    // JSONObject to JavaObject，校验通过
    JSONObject.of("longitude", 120.1552, "latitude", 30.2741)
            .to(Point2.class);

    // parseObject 校验失败，longitude超过最大值
    assertThrows(JSONSchemaValidException.class, () ->
            JSON.parseObject("{\"longitude\":220.1552,\"latitude\":30.2741}", Point2.class)
    );

    // 校验JSONObject失败，longitude超过最大值
    assertThrows(JSONSchemaValidException.class, () ->
            JSONObject.of("longitude", 220.1552, "latitude", 30.2741)
                    .to(Point2.class)
    );
}
```
