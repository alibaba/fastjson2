package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSerializeTest {
    class Vehicle {
        private String type;

        public Vehicle(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    class Car
            extends Vehicle {
        private int seats;

        public Car(String type, int seats) {
            super(type);
            this.seats = seats;
        }

        public int getSeats() {
            return seats;
        }
    }

    class Garage {
        @JsonSerialize(contentAs = Vehicle.class)
        private List<Vehicle> vehicles = new ArrayList<>();

        public void addVehicle(Vehicle v) {
            vehicles.add(v);
        }

        public List<Vehicle> getVehicles() {
            return vehicles;
        }
    }

    @Test
    public void test() throws Exception {
        Garage garage = new Garage();
        garage.addVehicle(new Car("Sedan", 5));
        garage.addVehicle(new Car("SUV", 7));

        ObjectMapper mapper = new ObjectMapper();
        String jackson = mapper.writeValueAsString(garage);
        assertEquals("{\"vehicles\":[{\"type\":\"Sedan\"},{\"type\":\"SUV\"}]}", jackson);
        assertEquals(jackson, JSON.toJSONString(garage));
    }

    class GarageMap {
        @JsonSerialize(contentAs = Vehicle.class)
        private Map<String, Vehicle> vehicles = new LinkedHashMap<>();

        public void addVehicle(Vehicle v) {
            vehicles.put(v.getType(), v);
        }

        public Map<String, Vehicle> getVehicles() {
            return vehicles;
        }
    }

    @Test
    public void testMap() throws Exception {
        GarageMap garage = new GarageMap();
        garage.addVehicle(new Car("Sedan", 5));
        garage.addVehicle(new Car("SUV", 7));

        ObjectMapper mapper = new ObjectMapper();
        String jackson = mapper.writeValueAsString(garage);
        assertEquals("{\"vehicles\":{\"Sedan\":{\"type\":\"Sedan\"},\"SUV\":{\"type\":\"SUV\"}}}", jackson);
        assertEquals(jackson, JSON.toJSONString(garage));
    }

    class GarageField {
        @JsonSerialize(contentAs = Vehicle.class)
        public List<Vehicle> vehicles = new ArrayList<>();

        public void addVehicle(Vehicle v) {
            vehicles.add(v);
        }
    }

    @Test
    public void testField() throws Exception {
        GarageField garage = new GarageField();
        garage.addVehicle(new Car("Sedan", 5));
        garage.addVehicle(new Car("SUV", 7));

        ObjectMapper mapper = new ObjectMapper();
        String jackson = mapper.writeValueAsString(garage);
        assertEquals("{\"vehicles\":[{\"type\":\"Sedan\"},{\"type\":\"SUV\"}]}", jackson);
        assertEquals(jackson, JSON.toJSONString(garage));
    }

    class GarageMapField {
        @JsonSerialize(contentAs = Vehicle.class)
        public Map<String, Vehicle> vehicles = new LinkedHashMap<>();

        public void addVehicle(Vehicle v) {
            vehicles.put(v.getType(), v);
        }
    }

    @Test
    public void testMapField() throws Exception {
        GarageMapField garage = new GarageMapField();
        garage.addVehicle(new Car("Sedan", 5));
        garage.addVehicle(new Car("SUV", 7));

        ObjectMapper mapper = new ObjectMapper();
        String jackson = mapper.writeValueAsString(garage);
        assertEquals("{\"vehicles\":{\"Sedan\":{\"type\":\"Sedan\"},\"SUV\":{\"type\":\"SUV\"}}}", jackson);
        assertEquals(jackson, JSON.toJSONString(garage));
    }
}
