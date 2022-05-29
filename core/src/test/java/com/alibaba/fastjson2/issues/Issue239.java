package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.schema.JSONSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue239 {
    @Test
    public void test() {
        // 定义必须包含longitude和latitude两个属性，其中longitude的取值范围是[-180 ~ 180]，latitude的取值范围是[-90, 90]
        JSONSchema schema = JSONSchema.parseSchema("{\n" +
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

    public static class Point1 {
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

    @JSONType(schema = "{'properties':{'longitude':{'type':'number','minimum':-180,'maximum':180},'latitude':{'type':'number','minimum':-90,'maximum':90}}}")
    public static class Point2 {
        @JSONField(schema = "{'minimum':-180,'maximum':180}")
        public float longitude;

        @JSONField(schema = "{'minimum':-90,'maximum':90}")
        public float latitude;
    }
}
