package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WriteClassNameWithFilterTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 101;
        ValueFilter filter = (Object object, String name, Object value) -> value;

        String str = JSON.toJSONString(bean, filter, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"@type\":\"com.alibaba.fastjson2.features.WriteClassNameWithFilterTest$Bean\",\"id\":101}", str);
    }

    @Test
    public void testAnnotationSerializeFeaturesWithFilter() {
        Circle circle = new Circle();
        circle.setType("circle");
        circle.setRadius(10);

        ValueFilter filter = (Object object, String name, Object value) -> value;

        String str = JSON.toJSONString(circle, filter);

        assertTrue(str.contains("\"type\":\"circle\""), "Should contain type:circle, actual: " + str);
        assertTrue(str.contains("\"radius\":10"), "Should contain radius:10, actual: " + str);
    }

    public static class Bean {
        public Integer id;
    }

    @JSONType(typeKey = "type")
    public static class Shape {
        private String type;

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    @JSONType(typeKey = "type", typeName = "circle", serializeFeatures = JSONWriter.Feature.WriteClassName)
    public static class Circle extends Shape {
        private int radius;

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }
    }
}
