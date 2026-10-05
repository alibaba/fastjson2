package com.alibaba.fastjson2.support.spring.data.mongodb;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeoJsonPolygonWriterRegressionTest {
    @Test
    public void preservesDistinctVertices() {
        GeoJsonPolygon polygon = new GeoJsonPolygon(
                new Point(0, 0), new Point(3, 0), new Point(0, 3), new Point(0, 0));
        String expected = "{\"type\":\"Polygon\",\"coordinates\":[[[0.0,0.0],[3.0,0.0],[0.0,3.0],[0.0,0.0]]]}";
        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            GeoJsonWriterModule.GeoJsonPolygonWriter.INSTANCE.write(writer, polygon, null, null, 0);
            assertEquals(expected, writer.toString());
        }
        try (JSONWriter writer = JSONWriter.ofUTF16()) {
            GeoJsonWriterModule.GeoJsonPolygonWriter.INSTANCE.write(writer, polygon, null, null, 0);
            assertEquals(expected, writer.toString());
        }
    }
}
