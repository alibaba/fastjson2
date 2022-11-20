package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.core.ObjectCodec;
import com.alibaba.fastjson2.adapter.jackson.core.Version;
import com.alibaba.fastjson2.adapter.jackson.databind.deser.std.StdDeserializer;
import com.alibaba.fastjson2.adapter.jackson.databind.module.SimpleModule;
import com.alibaba.fastjson2.adapter.jackson.databind.ser.std.StdSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectMapperTest2 {
    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module =
                new SimpleModule("CustomCarSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(Car.class, new CustomCarSerializer());
        mapper.registerModule(module);
        Car car = new Car("yellow", "renault");
        String carJson = mapper.writeValueAsString(car);
        assertEquals("{\"car_brand\":\"renault\"}", carJson);
    }

    public static class CustomCarSerializer
            extends StdSerializer<Car> {
        public CustomCarSerializer() {
            this(null);
        }

        public CustomCarSerializer(Class<Car> t) {
            super(t);
        }

        @Override
        public void serialize(
                Car car, JsonGenerator jsonGenerator, SerializerProvider serializer) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("car_brand", car.getType());
            jsonGenerator.writeEndObject();
        }
    }

    @Test
    public void test1() {
        String json = "{ \"color\" : \"Black\", \"type\" : \"BMW\" }";
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module =
                new SimpleModule("CustomCarDeserializer", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(Car.class, new CustomCarDeserializer());
        mapper.registerModule(module);
        Car car = mapper.readValue(json, Car.class);
        assertNotNull(car);
        assertEquals("Black", car.getColor());
    }

    public class CustomCarDeserializer
            extends StdDeserializer<Car> {
        public CustomCarDeserializer() {
            this(null);
        }

        public CustomCarDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Car deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
            Car car = new Car();
            ObjectCodec codec = parser.getCodec();
            JsonNode node = codec.readTree(parser);

            // try catch block
            JsonNode colorNode = node.get("color");
            String color = colorNode.asText();
            car.setColor(color);
            return car;
        }
    }

    @Test
    public void test2() {
        URL url = ObjectMapperTest2.class.getClassLoader().getResource("data/car.json");
        ObjectMapper mapper = new ObjectMapper();
        Car car = mapper.readValue(url, Car.class);
        assertNotNull(car);
        assertEquals("red", car.color);
    }

    public static class Car {
        private String color;
        private String type;

        public Car() {
        }

        public Car(String color, String type) {
            this.color = color;
            this.type = type;
        }

        public String getColor() {
            return color;
        }

        public String getType() {
            return type;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
