package com.alibaba.fastjson.geo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.geo.Geometry;
import com.alibaba.fastjson.support.geo.Polygon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PolygonTest {
    @Test
    public void test_geo() {
        String str = "{\n" +
                "    \"type\": \"Polygon\",\n" +
                "    \"coordinates\": [\n" +
                "        [\n" +
                "            [100.0, 0.0],\n" +
                "            [101.0, 0.0],\n" +
                "            [101.0, 1.0],\n" +
                "            [100.0, 1.0],\n" +
                "            [100.0, 0.0]\n" +
                "        ],\n" +
                "        [\n" +
                "            [100.8, 0.8],\n" +
                "            [100.8, 0.2],\n" +
                "            [100.2, 0.2],\n" +
                "            [100.2, 0.8],\n" +
                "            [100.8, 0.8]\n" +
                "        ]\n" +
                "    ]\n" +
                "}";

        Geometry geometry = JSON.parseObject(str, Geometry.class);
        assertEquals(Polygon.class, geometry.getClass());

        assertEquals("{\"type\":\"Polygon\",\"coordinates\":[[[100.0,0.0],[101.0,0.0],[101.0,1.0],[100.0,1.0],[100.0,0.0]],[[100.8,0.8],[100.8,0.2],[100.2,0.2],[100.2,0.8],[100.8,0.8]]]}", JSON.toJSONString(geometry));

        String str2 = JSON.toJSONString(geometry);
        assertEquals(str2, JSON.toJSONString(JSON.parseObject(str2, Geometry.class)));
    }
}
