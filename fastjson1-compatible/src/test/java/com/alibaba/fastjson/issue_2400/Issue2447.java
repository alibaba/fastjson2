package com.alibaba.fastjson.issue_2400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2447 {
    @Test
    public void test_for_issue() {
        VO vo = new VO();
        vo.id = 123;
        vo.location = new Location(127, 37);

        Object obj = JSON.toJSON(vo);
        String text = JSON.toJSONString(obj, SerializerFeature.SortField);
        assertEquals("{\"latitude\":37,\"id\":123,\"longitude\":127}", text);
    }

    @Test
    public void test_for_issue2() {
        VO2 vo = new VO2();
        vo.id = 123;
        vo.properties.put("latitude", 37);
        vo.properties.put("longitude", 127);

        Object obj = JSON.toJSON(vo);
        String text = JSON.toJSONString(obj, SerializerFeature.SortField);
        assertEquals("{\"latitude\":37,\"id\":123,\"longitude\":127}", text);
    }

    public static class VO {
        public int id;

        @JSONField(unwrapped = true)
        public Location location;
    }

    public static class VO2 {
        public int id;

        @JSONField(unwrapped = true)
        public Map<String, Object> properties = new LinkedHashMap<String, Object>();
    }

    public static class Location {
        public int longitude;
        public int latitude;

        public Location() {
        }

        public Location(int longitude, int latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }
}
