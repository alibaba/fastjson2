package com.alibaba.fastjson2.support.spring.data.mongodb;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.Fnv;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.lang.reflect.Type;

public class GeoJsonPointReader
        implements ObjectReader<GeoJsonPoint> {
    public static final GeoJsonPointReader INSTANCE = new GeoJsonPointReader();

    static final long HASH_TYPE = Fnv.hashCode64("type");
    static final long HASH_POINT = Fnv.hashCode64("Point");
    static final long HASH_COORDINATES = Fnv.hashCode64("coordinates");

    public GeoJsonPointReader() {
    }

    @Override
    public GeoJsonPoint readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        jsonReader.nextIfObjectStart();

        double x = 0, y = 0;
        for (; ; ) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long nameHashCode = jsonReader.readFieldNameHashCode();
            if (nameHashCode == HASH_TYPE) {
                long valueHashCode = jsonReader.readValueHashCode();
                if (valueHashCode != HASH_POINT) {
                    throw new JSONException("not support input type : " + jsonReader.getString());
                }
            } else if (nameHashCode == HASH_COORDINATES) {
                boolean match = jsonReader.nextIfMatch('[');
                if (!match) {
                    throw new JSONException("coordinates not support input " + jsonReader.current());
                }

                x = jsonReader.readDoubleValue();
                y = jsonReader.readDoubleValue();

                match = jsonReader.nextIfMatch(']');
                if (!match) {
                    throw new JSONException("coordinates not support input " + jsonReader.current());
                }
                jsonReader.nextIfMatch(',');
            } else {
                jsonReader.skipValue();
            }
        }

        jsonReader.nextIfMatch(',');
        return new GeoJsonPoint(x, y);
    }
}
