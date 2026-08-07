package com.alibaba.fastjson2.geo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.support.geo.Geometry;
import com.alibaba.fastjson2.support.geo.MultiLineString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiLineStringTest {
    @Test
    public void test_geo() throws Exception {
        String str = "{\n" +
                "    \"type\": \"MultiLineString\",\n" +
                "    \"coordinates\": [\n" +
                "        [\n" +
                "            [100.0, 0.0],\n" +
                "            [101.0, 1.0]\n" +
                "        ],\n" +
                "        [\n" +
                "            [102.0, 2.0],\n" +
                "            [103.0, 3.0]\n" +
                "        ]\n" +
                "    ]\n" +
                "}";

        Geometry geometry = JSON.parseObject(str, Geometry.class);
        assertEquals(MultiLineString.class, geometry.getClass());

        assertEquals("{\"type\":\"MultiLineString\",\"coordinates\":[[[100.0,0.0],[101.0,1.0]],[[102.0,2.0],[103.0,3.0]]]}", JSON.toJSONString(geometry));

        String str2 = JSON.toJSONString(geometry);
        assertEquals(str2, JSON.toJSONString(JSON.parseObject(str2, Geometry.class)));
    }
}
