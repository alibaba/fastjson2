package com.alibaba.fastjson2.adapter.gson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonTest {
    @Test
    public void test() {
        Gson gson = new Gson();
        Car car = gson.fromJson("{\"color\":\"red\"}", Car.class);
        assertEquals("red", car.color);
        assertEquals("{\"color\":\"red\"}", gson.toJson(car));
    }

    public static class Car {
        public String color;
        public String type;
    }
}
