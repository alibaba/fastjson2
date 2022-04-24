package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.time.Instant;
import java.util.Map;

public final class ObjectReaderImplInstant implements ObjectReader {
    public static final ObjectReaderImplInstant INSTANCE = new ObjectReaderImplInstant();

    @Override
    public Object createInstance(Map map) {
        Number nano = (Number) map.get("nano");
        Number epochSecond = (Number) map.get("epochSecond");

        if (nano != null && epochSecond != null) {
            return Instant.ofEpochSecond(epochSecond.longValue(), nano.longValue());
        }

        if (epochSecond != null) {
            return Instant.ofEpochSecond(epochSecond.longValue());
        }

        Number epochMilli = (Number) map.get("epochMilli");
        if (epochMilli != null) {
            return Instant.ofEpochMilli(epochMilli.longValue());
        }

        throw new JSONException("can not create instant.");
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        return jsonReader.readInstant();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        return jsonReader.readInstant();
    }
}
