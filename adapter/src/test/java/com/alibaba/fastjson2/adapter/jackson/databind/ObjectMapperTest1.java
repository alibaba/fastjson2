package com.alibaba.fastjson2.adapter.jackson.databind;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectMapperTest1 {
    @Test
    public void test() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        String carJson = "{ \"brand\":\"Toyota\", \"doors\":null }";
        assertThrows(
                Exception.class,
                () -> objectMapper.readValue(carJson, Car.class)
        );
    }

    public class Car {
        private String brand;
        private int doors;

        public String getBrand() {
            return this.brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public int getDoors() {
            return this.doors;
        }

        public void setDoors(int doors) {
            this.doors = doors;
        }
    }
}
