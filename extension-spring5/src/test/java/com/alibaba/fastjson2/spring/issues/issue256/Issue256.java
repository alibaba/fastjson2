package com.alibaba.fastjson2.spring.issues.issue256;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.support.spring.data.mongodb.GeoJsonReaderModule;
import com.alibaba.fastjson2.support.spring.data.mongodb.GeoJsonWriterModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonMultiPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue256 {
    @BeforeEach
    public void init() {
        JSON.register(GeoJsonReaderModule.INSTANCE);
        JSON.register(GeoJsonWriterModule.INSTANCE);
    }

    @Test
    public void test() {
        String str = "{\n" +
                "    \"_id\":\n" +
                "    {\n" +
                "        \"$oid\": \"61a591ef96eb001a9437d6c4\"\n" +
                "    },\n" +
                "    \"gpsAddress\":\n" +
                "    {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\":\n" +
                "        [\n" +
                "            114.027285886961,\n" +
                "            22.6741582981732\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean.gpsAddress);
        assertEquals(114.027285886961D, bean.gpsAddress.getX());
        assertEquals(22.6741582981732D, bean.gpsAddress.getY());

        assertEquals("{\"gpsAddress\":{\"type\":\"Point\",\"coordinates\":[114.027285886961,22.6741582981732]}}", JSON.toJSONString(bean));
    }

    @Test
    public void testPoint() {
        String str = "[100.0, 0.0]";

        Point point = JSON.parseObject(str, Point.class);
        assertEquals(100, point.getX());
        assertEquals(0, point.getY());

        assertEquals("[100.0,0.0]", JSON.toJSONString(point));
    }

    @Test
    public void testGeoPolygon() {
        String str = "{\n" +
                "    \"type\": \"Polygon\",\n" +
                "    \"coordinates\": [\n" +
                "        [\n" +
                "            [100.0, 0.0],\n" +
                "            [101.0, 0.0],\n" +
                "            [101.0, 1.0],\n" +
                "            [100.0, 1.0],\n" +
                "            [100.0, 0.0]\n" +
                "        ]\n" +
                "    ]\n" +
                "}";

        GeoJsonPolygon polygon = JSON.parseObject(str, GeoJsonPolygon.class);
        GeoJsonLineString line = polygon.getCoordinates().get(0);
        Point point = line.getCoordinates().get(0);
        assertEquals(100, point.getX());
        assertEquals(0, point.getY());

        assertEquals("{\"type\":\"Polygon\",\"coordinates\":[[[100.0,0.0],[100.0,0.0],[100.0,0.0],[100.0,0.0],[100.0,0.0]]]}", JSON.toJSONString(polygon));
    }

    @Test
    public void testLineString() {
        String str = "{\n" +
                "    \"type\": \"LineString\",\n" +
                "    \"coordinates\": [\n" +
                "        [100.0, 0.0],\n" +
                "        [101.0, 1.0]\n" +
                "    ]\n" +
                "}";

        GeoJsonLineString lineString = JSON.parseObject(str, GeoJsonLineString.class);
        assertEquals(2, lineString.getCoordinates().size());
        assertEquals(100, lineString.getCoordinates().get(0).getX());
        assertEquals(0, lineString.getCoordinates().get(0).getY());
        assertEquals(101, lineString.getCoordinates().get(1).getX());
        assertEquals(1, lineString.getCoordinates().get(1).getY());

        assertEquals("{\"type\":\"LineString\",\"coordinates\":[[100.0,0.0],[101.0,1.0]]}", JSON.toJSONString(lineString));
    }

    @Test
    public void testMultiPoint() {
        String str = "{\n" +
                "         \"type\": \"MultiPoint\",\n" +
                "         \"coordinates\": [\n" +
                "             [100.0, 0.0],\n" +
                "             [101.0, 1.0]\n" +
                "         ]\n" +
                "     }";

        GeoJsonMultiPoint multiPoint = JSON.parseObject(str, GeoJsonMultiPoint.class);
        assertEquals(2, multiPoint.getCoordinates().size());
        assertEquals(100, multiPoint.getCoordinates().get(0).getX());
        assertEquals(0, multiPoint.getCoordinates().get(0).getY());
        assertEquals(101, multiPoint.getCoordinates().get(1).getX());
        assertEquals(1, multiPoint.getCoordinates().get(1).getY());

        assertEquals("{\"type\":\"MultiPoint\",\"coordinates\":[[100.0,0.0],[101.0,1.0]]}", JSON.toJSONString(multiPoint));
    }

    public static class Bean {
        public GeoJsonPoint gpsAddress;
    }
}
