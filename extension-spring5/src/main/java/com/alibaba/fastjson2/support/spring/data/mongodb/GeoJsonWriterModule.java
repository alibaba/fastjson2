package com.alibaba.fastjson2.support.spring.data.mongodb;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonMultiPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GeoJsonWriterModule
        implements ObjectWriterModule {
    public static final GeoJsonWriterModule INSTANCE = new GeoJsonWriterModule();

    @Override
    public void init(ObjectWriterProvider provider) {
        provider.register(GeoJsonPoint.class, GeoJsonPointWriter.INSTANCE);
        provider.register(Point.class, PointWriter.INSTANCE);
        provider.register(GeoJsonPolygon.class, GeoJsonPolygonWriter.INSTANCE);
        provider.mixIn(GeoJsonLineString.class, GeoJsonLineStringMixin.class);
        provider.mixIn(GeoJsonMultiPoint.class, GeoJsonMultiPointMixin.class);
    }

    @JSONType(orders = {"type", "coordinates"})
    static class GeoJsonLineStringMixin {
        public String getType() {
            return null;
        }

        public List<GeoJsonLineString> getCoordinates() {
            return null;
        }
    }

    @JSONType(includes = {"type", "coordinates"}, orders = {"type", "coordinates"})
    static class GeoJsonMultiPointMixin {
        public String getType() {
            return null;
        }

        public List<Point> getCoordinates() {
            return null;
        }
    }

    static class GeoJsonPolygonWriter
            implements ObjectWriter {
        public static final GeoJsonPolygonWriter INSTANCE = new GeoJsonPolygonWriter();

        static final String PREFIX = "{\"type\":\"Polygon\",\"coordinates\":";
        static final byte[] utf8Prefix = PREFIX.getBytes(StandardCharsets.US_ASCII);
        static final char[] charsPrefix = PREFIX.toCharArray();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            GeoJsonPolygon polygon = (GeoJsonPolygon) object;
            if (jsonWriter.utf8) {
                jsonWriter.writeRaw(utf8Prefix);
            } else if (jsonWriter.utf16) {
                jsonWriter.writeRaw(charsPrefix);
            } else {
                jsonWriter.startObject();
                jsonWriter.writeName("type");
                jsonWriter.writeColon();
                jsonWriter.writeString("Point");

                jsonWriter.writeName("coordinates");
                jsonWriter.writeColon();
            }

            List<GeoJsonLineString> coordinates = polygon.getCoordinates();
            jsonWriter.startArray();
            for (int i = 0; i < coordinates.size(); i++) {
                if (i != 0) {
                    jsonWriter.writeComma();
                }
                GeoJsonLineString lineString = coordinates.get(i);
                jsonWriter.startArray();
                List<Point> points = lineString.getCoordinates();
                for (int j = 0; j < points.size(); j++) {
                    if (j != 0) {
                        jsonWriter.writeComma();
                    }
                    Point point = points.get(i);
                    jsonWriter.writeDoubleArray(point.getX(), point.getY());
                }
                jsonWriter.endArray();
            }
            jsonWriter.endArray();

            jsonWriter.endObject();
        }
    }

    static class PointWriter
            implements ObjectWriter {
        public static final PointWriter INSTANCE = new PointWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            Point point = (Point) object;
            jsonWriter.writeDoubleArray(point.getX(), point.getY());
        }
    }

    static class GeoJsonPointWriter
            implements ObjectWriter {
        public static final GeoJsonPointWriter INSTANCE = new GeoJsonPointWriter();

        static final String PREFIX = "{\"type\":\"Point\",\"coordinates\":";
        static final byte[] utf8Prefix = PREFIX.getBytes(StandardCharsets.US_ASCII);
        static final char[] charsPrefix = PREFIX.toCharArray();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            GeoJsonPoint point = (GeoJsonPoint) object;
            if (jsonWriter.utf8) {
                jsonWriter.writeRaw(utf8Prefix);
            } else if (jsonWriter.utf16) {
                jsonWriter.writeRaw(charsPrefix);
            } else {
                jsonWriter.startObject();
                jsonWriter.writeName("type");
                jsonWriter.writeColon();
                jsonWriter.writeString("Point");

                jsonWriter.writeName("coordinates");
                jsonWriter.writeColon();
            }

            jsonWriter.writeDoubleArray(point.getX(), point.getY());

            jsonWriter.endObject();
        }
    }
}
