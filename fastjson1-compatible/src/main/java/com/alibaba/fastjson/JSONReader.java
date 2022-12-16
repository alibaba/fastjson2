package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import java.util.TimeZone;

public class JSONReader
        implements Closeable {
    private final Reader input;
    private final com.alibaba.fastjson2.JSONReader raw;

    public JSONReader(Reader reader) {
        this(reader, new Feature[0]);
    }

    public JSONReader(Reader input, Feature... features) {
        com.alibaba.fastjson2.JSONReader.Context context = JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE, features);
        this.raw = com.alibaba.fastjson2.JSONReader.of(input, context);
        this.input = input;
        for (Feature feature : features) {
            if (feature == Feature.SupportArrayToBean) {
                context.config(com.alibaba.fastjson2.JSONReader.Feature.SupportArrayToBean);
            }
        }
    }

    public void setLocale(Locale locale) {
        raw.getContext().setLocale(locale);
    }

    public void setTimzeZone(TimeZone timezone) {
        raw.getContext().setTimeZone(timezone);
    }

    public <T> T readObject(Class<T> type) {
        raw.nextIfMatch(':');
        try {
            return raw.read(type);
        } catch (com.alibaba.fastjson2.JSONException ex) {
            throw new JSONException(ex.getMessage(), ex.getCause());
        }
    }

    public Object readObject() {
        raw.nextIfMatch(':');
        return raw.readAny();
    }

    public void readObject(Object object) {
        raw.nextIfMatch(':');
        raw.readObject(object);
    }

    public Integer readInteger() {
        raw.nextIfMatch(':');
        return raw.readInt32();
    }

    public Long readLong() {
        raw.nextIfMatch(':');
        return raw.readInt64();
    }

    public String readString() {
        raw.nextIfMatch(':');
        return raw.readString();
    }

    public boolean hasNext() {
        if (raw.isEnd()) {
            return false;
        }

        char ch = raw.current();
        return ch != ']' && ch != '}';
    }

    public void startArray() {
        raw.nextIfMatch(':');
        if (!raw.nextIfMatch('[')) {
            throw new JSONException("not support operation");
        }
    }

    public void endArray() {
        if (!raw.nextIfMatch(']')) {
            throw new JSONException("not support operation");
        }
        raw.nextIfMatch(',');
    }

    public void startObject() {
        raw.nextIfMatch(':');
        if (!raw.nextIfMatch('{')) {
            throw new JSONException("not support operation");
        }
    }

    public void endObject() {
        if (!raw.nextIfMatch('}')) {
            throw new JSONException(raw.info("not support operation"));
        }
        raw.nextIfMatch(',');
    }

    public Locale getLocal() {
        return raw.getContext().getLocale();
    }

    public TimeZone getTimeZone() {
        return raw.getContext().getTimeZone();
    }

    public void config(Feature feature, boolean state) {
        com.alibaba.fastjson2.JSONReader.Feature rawFeature = null;
        switch (feature) {
            case SupportArrayToBean:
                rawFeature = com.alibaba.fastjson2.JSONReader.Feature.SupportArrayToBean;
                break;
            case UseNativeJavaObject:
                rawFeature = com.alibaba.fastjson2.JSONReader.Feature.UseNativeObject;
                break;
            case SupportAutoType:
                rawFeature = com.alibaba.fastjson2.JSONReader.Feature.SupportAutoType;
                break;
            default:
                break;
        }

        if (rawFeature == null) {
            return;
        }
        raw.getContext().config(rawFeature, state);
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
