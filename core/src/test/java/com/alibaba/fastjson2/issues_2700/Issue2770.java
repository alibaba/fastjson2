package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2770 {
    @Test
    public void testToJSONString() {
        String str = JSON.toJSONString(DataType.STRING);
        assertEquals("\"STRING\"", str);

        Car car = new Car();
        car.dataType = DataType.STRING;
        assertEquals("{\"dataType\":\"STRING\"}", JSON.toJSONString(car));
    }

    public static class Car {
        public DataType dataType;
    }

    public enum DataType {
        STRING(DataTypeCategory.STRING, "string", "字符串", "xxxx"),
        ARRAY_STRING(DataTypeCategory.ARRAY, "array_string", "字符串数组", "[Array string]");

        private final DataTypeCategory dataTypeCategory;

        @JsonProperty
        private final String code;
        @JsonProperty
        private final String description;

        private final String sample;

        DataType(DataTypeCategory dataTypeCategory, String code, String description, String sample) {
            this.dataTypeCategory = dataTypeCategory;
            this.code = code;
            this.description = description;
            this.sample = sample;
        }

        public DataTypeCategory getDataTypeCategory() {
            return this.dataTypeCategory;
        }

        public String getCode() {
            return this.code;
        }

        public String getDescription() {
            return this.description;
        }

        public String getSample() {
            return sample;
        }
    }

    public static enum DataTypeCategory {
        STRING,
        ARRAY;
    }
}
