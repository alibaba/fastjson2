package com.alibaba.fastjson2.geo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.support.geo.Geometry;
import com.alibaba.fastjson2.support.geo.LineString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LineStringTest {
    @Test
    public void test_geo() throws Exception {
        String str = "{\n" +
                "    \"type\": \"LineString\",\n" +
                "    \"coordinates\": [\n" +
                "        [100.0, 0.0],\n" +
                "        [101.0, 1.0]\n" +
                "    ]\n" +
                "}";

        Geometry geometry = JSON.parseObject(str, Geometry.class);
        assertEquals(LineString.class, geometry.getClass());

        assertEquals("{\"type\":\"LineString\",\"coordinates\":[[100.0,0.0],[101.0,1.0]]}", JSON.toJSONString(geometry));

        String str2 = JSON.toJSONString(geometry);
        assertEquals(str2, JSON.toJSONString(JSON.parseObject(str2, Geometry.class)));
    }
}
