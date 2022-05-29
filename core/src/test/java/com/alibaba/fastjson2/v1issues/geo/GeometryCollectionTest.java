package com.alibaba.fastjson2.v1issues.geo;

import com.alibaba.fastjson.support.geo.Geometry;
import com.alibaba.fastjson.support.geo.GeometryCollection;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeometryCollectionTest {
    @Test
    public void test_geo() {
        String str = "{\n" +
                "    \"type\": \"GeometryCollection\",\n" +
                "    \"geometries\": [{\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [100.0, 0.0]\n" +
                "    }, {\n" +
                "    \"type\": \"LineString\",\n" +
                "    \"coordinates\": [\n" +
                "        [101.0, 0.0],\n" +
                "        [102.0, 1.0]\n" +
                "    ]\n" +
                "    }]\n" +
                "}";

        Geometry geometry = JSON.parseObject(str, Geometry.class);
        assertEquals(GeometryCollection.class, geometry.getClass());

        assertEquals(
                "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\",\"coordinates\":[100.0,0.0]},{\"type\":\"LineString\",\"coordinates\":[[101.0,0.0],[102.0,1.0]]}]}",
                JSON.toJSONString(geometry));

        String str2 = JSON.toJSONString(geometry);
        assertEquals(str2, JSON.toJSONString(JSON.parseObject(str2, Geometry.class)));
    }
}
