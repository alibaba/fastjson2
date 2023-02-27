package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1147 {
    @Test
    public void test_for_issue() {
        VO vo = new VO();
        vo.id = 123;
        vo.location = new Location(127, 37);

        String expected = "{\"latitude\":37,\"longitude\":127,\"id\":123}";
        String expectedStr = "VO{id=123, location=Location{longitude=127, latitude=37}}";
        String text = JSON.toJSONString(vo);
        assertEquals(127, vo.location.longitude);
        assertEquals(37, vo.location.latitude);
        assertEquals(expected, text);
        assertEquals(expectedStr, vo.toString());

        VO obj = JSON.parseObject(expected, VO.class);
        assertEquals(127, obj.location.longitude);
        assertEquals(37, obj.location.latitude);
        assertEquals(expectedStr, obj.toString());
        assertEquals(expected, JSON.toJSONString(obj));
    }

    @Test
    public void test_for_issue2() {
        VO2 vo = new VO2();
        vo.id = 123;
        vo.properties.put("latitude", 37);
        vo.properties.put("longitude", 127);

        String expected = "{\"latitude\":37,\"longitude\":127,\"id\":123}";
        String expectedStr = "VO2{id=123, properties={latitude=37, longitude=127}}";
        String text = JSON.toJSONString(vo);
        assertEquals(expected, text);
        assertEquals(expectedStr, vo.toString());

        VO2 obj = JSON.parseObject(expected, VO2.class);
        assertEquals(expectedStr, obj.toString());
        assertEquals(expected, JSON.toJSONString(obj));
    }

    public static class VO {
        @JSONField(ordinal = 1)
        private int id;

        @JSONField(unwrapped = true)
        private Location location = new Location();

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return "VO{" +
                    "id=" + id +
                    ", location=" + location +
                    '}';
        }
    }

    public static class Location {
        private int longitude;
        private int latitude;

        public Location() {
        }

        public Location(int longitude, int latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public int getLongitude() {
            return longitude;
        }

        public void setLongitude(int longitude) {
            this.longitude = longitude;
        }

        public int getLatitude() {
            return latitude;
        }

        public void setLatitude(int latitude) {
            this.latitude = latitude;
        }

        @Override
        public String toString() {
            return "Location{" +
                    "longitude=" + longitude +
                    ", latitude=" + latitude +
                    '}';
        }
    }

    public static class VO2 {
        @JSONField(ordinal = 1)
        private int id;

        @JSONField(unwrapped = true)
        private Map<String, Object> properties = new LinkedHashMap<>();

        @JSONField(unwrapped = true)
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }

        @Override
        public String toString() {
            return "VO2{" +
                    "id=" + id +
                    ", properties=" + properties +
                    '}';
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }
}
