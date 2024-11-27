package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2981 {
    @Test
    public void test() {
        Shape.Circle circle = new Shape.Circle();
        circle.setRadius(5);
        assertEquals(JSON.parse(JSON.toJSONString(circle)), JSON.toJSON(circle));
    }

    @JSONType(typeKey = "type", seeAlso = {Shape.Circle.class})
    public static class Shape {
        @JSONType(typeKey = "type", typeName = "circle", serializeFeatures = JSONWriter.Feature.WriteClassName)
        public static class Circle
                extends Shape {
            private int radius;
            public int getRadius() { return radius; }
            public void setRadius(int radius) { this.radius = radius; }
        }
    }
}
