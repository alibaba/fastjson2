package com.alibaba.fastjson2.adapter.jackson.databind.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonSubTypes;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonTypeInfo;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonTypeName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTypeInfoTest {
    @Test
    public void test() throws Exception {
        Shape shape = new Circle("CustomCircle", 1);
        String str = JSON.toJSONString(shape, JSONWriter.Feature.PrettyFormat);
        assertEquals("{\n" +
                "\t\"@type\":\"circle\",\n" +
                "\t\"name\":\"CustomCircle\",\n" +
                "\t\"radius\":1.0\n" +
                "}", str);

        Shape shape1 = JSON.parseObject(str, Shape.class);
        assertEquals(shape.getClass(), shape1.getClass());

        Circle cycle = (Circle) shape;
        Circle cycle1 = (Circle) shape1;
        assertEquals(cycle.name, cycle1.name);
        assertEquals(cycle.radius, cycle1.radius);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Square.class, name = "square"),
            @JsonSubTypes.Type(value = Circle.class, name = "circle")
    })
    static class Shape {
        public String name;

        Shape(String name) {
            this.name = name;
        }
    }

    @JsonTypeName("square")
    static class Square
            extends Shape {
        public double length;

        Square() {
            this(null, 0.0);
        }

        Square(String name, double length) {
            super(name);
            this.length = length;
        }
    }

    @JsonTypeName("circle")
    static class Circle
            extends Shape {
        public double radius;

        Circle() {
            this(null, 0.0);
        }

        Circle(String name, double radius) {
            super(name);
            this.radius = radius;
        }
    }
}
