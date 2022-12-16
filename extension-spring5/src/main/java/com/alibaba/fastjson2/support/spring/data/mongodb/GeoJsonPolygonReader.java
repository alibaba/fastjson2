package com.alibaba.fastjson2.support.spring.data.mongodb;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.Fnv;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonPolygonReader
        implements ObjectReader<GeoJsonPolygon> {
    public static final GeoJsonPolygonReader INSTANCE = new GeoJsonPolygonReader();

    static final long HASH_TYPE = Fnv.hashCode64("type");
    static final long HASH_POLYGON = Fnv.hashCode64("Polygon");
    static final long HASH_COORDINATES = Fnv.hashCode64("coordinates");

    public GeoJsonPolygonReader() {
    }

    @Override
    public GeoJsonPolygon readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        jsonReader.nextIfObjectStart();

        List<Point> points = new ArrayList<>();

        for (; ; ) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long nameHashCode = jsonReader.readFieldNameHashCode();
            if (nameHashCode == HASH_TYPE) {
                long valueHashCode = jsonReader.readValueHashCode();
                if (valueHashCode != HASH_POLYGON) {
                    throw new JSONException("not support input type : " + jsonReader.getString());
                }
            } else if (nameHashCode == HASH_COORDINATES) {
                GeoJsonLineString lineString = jsonReader.read(GeoJsonLineString.class);
                points.addAll(lineString.getCoordinates());
            } else {
                jsonReader.skipValue();
            }
        }

        jsonReader.nextIfMatch(',');
        return new GeoJsonPolygon(points);
    }
}
