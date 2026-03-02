package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;
import java.time.Instant;

public class InstantReader implements ObjectReader<Instant> {

    public static final InstantReader INSTANCE = new InstantReader();

    @Override
    public Instant readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return jsonReader.readInstant();
    }
}
