package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DWTest {
    @Test
    public void test_parse_object_with_array_field() {
        String json = "{\n" +
                "                       \"stores\": [\n" +
                "                           {\n" +
                "                               \"car\": {\n" +
                "                                   \"color\": {\n" +
                "                                       \"name\": \"red\"\n" +
                "                                   }\n" +
                "                               }\n" +
                "                           },\n" +
                "                           {\n" +
                "                               \"car\": {\n" +
                "                                   \"color\": [\n" +
                "                                       {\n" +
                "                                           \"name\": \"red\"\n" +
                "                                       },\n" +
                "                                       {\n" +
                "                                           \"name\": \"green\"\n" +
                "                                       }\n" +
                "                                   ]\n" +
                "                               }\n" +
                "                           }\n" +
                "                       ]\n" +
                "                   }";
        JSONObject chartObject = JSON.parseObject(json);
        JSONArray stores = chartObject.getJSONArray("stores");

        for (int i = 0; i < stores.size(); i++) {
            JSONObject store = stores.getJSONObject(i);

            Car car = JSON.parseObject(store.getString("car"), Car.class);
            System.out.println(JSON.toJSONString(car, JSONWriter.Feature.PrettyFormat));
        }
    }

    public static class Car {
        private Color color;

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void setColor(List<Color> colors) {
            this.color = colors.get(0);
        }
    }

    public static class Color {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
