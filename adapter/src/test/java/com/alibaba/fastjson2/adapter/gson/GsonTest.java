package com.alibaba.fastjson2.adapter.gson;

import com.alibaba.fastjson2.adapter.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonTest {
    @Test
    public void test() {
        Gson gson = new Gson();

        Car car = gson.fromJson("{\"color\":\"red\"}", Car.class);
        assertEquals("red", car.color);
        assertEquals("{\"color\":\"red\"}", gson.toJson(car));

        car = gson.fromJson("{\"color\":\"red\"}", (Type) Car.class);
        assertEquals("red", car.color);

        List<Car> cars = gson.fromJson("[{\"color\":\"blue\"}]", new TypeToken<List<Car>>(){});
        assertEquals(1, cars.size());
        assertEquals("blue", cars.get(0).color);
    }

    public static class Car {
        public String color;
    }
}
