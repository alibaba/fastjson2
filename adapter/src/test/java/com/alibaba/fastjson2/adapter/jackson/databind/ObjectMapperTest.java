package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectMapperTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void test() throws Exception {
        Car car = new Car("yellow", "renault");
        String json = objectMapper.writeValueAsString(car);
        assertEquals("{\"color\":\"yellow\",\"type\":\"renault\"}", json);

        Car car1 = objectMapper.readValue(json, Car.class);
        assertEquals(car.color, car1.color);
        assertEquals(car.type, car1.type);
    }

    @Test
    public void test1() {
        String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
        Map<String, Object> map
                = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        assertEquals("Black", map.get("color"));
    }

    public static class Car {
        public final String color;
        public final String type;

        public Car(String color, String type) {
            this.color = color;
            this.type = type;
        }
    }
}
