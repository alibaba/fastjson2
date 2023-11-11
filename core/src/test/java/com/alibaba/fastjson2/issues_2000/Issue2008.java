package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2008 {
    @Test
    public void testJson() {
        String str = "{\"name\":\"abc\", \"dataType\": \"STRING\", \"dataTypeCategory\":\"STRING\"}";
        // 此处断点反序列化出来的对象car中的属性dataType为null。（而使用fastjson1.2.83是正常的）
        Car car = JSON.parseObject(str, Car.class);
        assertNotNull(car.dataType);
        assertEquals(DataType.STRING, car.dataType);
        assertEquals(DataTypeCategory.STRING, car.dataTypeCategory);
    }

    public static class Car {
        private String name;
        private DataType dataType;

        private DataTypeCategory dataTypeCategory;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DataType getDataType() {
            return this.dataType;
        }

        public void setDataType(final DataType dataType) {
            this.dataType = dataType;
        }

        public DataTypeCategory getDataTypeCategory() {
            return dataTypeCategory;
        }

        public void setDataTypeCategory(DataTypeCategory dataTypeCategory) {
            this.dataTypeCategory = dataTypeCategory;
        }
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

    public enum DataTypeCategory {
        STRING,
        ARRAY;
    }
}
