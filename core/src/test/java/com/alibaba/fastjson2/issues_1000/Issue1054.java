package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1054 {
    @Test
    public void test() {
        SensorSoilTemperature temperature = JSONObject.parseObject("{\"value\":24.4801}", SensorSoilTemperature.class);
        assertNotNull(temperature);
        assertEquals(new BigDecimal("24.4801"), temperature.value);
    }

    @JSONType(
            serializeFeatures = JSONWriter.Feature.FieldBased,
            deserializeFeatures = {JSONReader.Feature.UseDefaultConstructorAsPossible, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportArrayToBean},
            ignores = {"dataTime", "id", "sensorId"}
    )
    public class SensorSoilTemperature
            extends SensorModel {
        @JSONField(name = "value")
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    public static class SensorModel {
    }

    @Test
    public void test1() {
        String str = "{\n" +
                "    \"success\": false,\n" +
                "    \"requestId\": \"requestId\",\n" +
                "    \"error\": [\n" +
                "        {\n" +
                "            \"code\": \"111\",\n" +
                "            \"message\": \"message\",\n" +
                "            \"longMessage\": \"--\",\n" +
                "            \"source\": \"666\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"data\": {\n" +
                "        \"requestString\": \"{\\\"scope\\\":\\\"1\\\",\\\"action\\\":\\\"1\\\",\\\"OrderNumber\\\":\\\"1\\\"}\"\n" +
                "    }\n" +
                "}";

        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean);
    }

    public static class Bean {
        public boolean success;
        public String requestId;
        public String data;
        public String error;
    }
}
