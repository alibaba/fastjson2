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
        this.raw = com.alibaba.fastjson2.JSONReader.of(input);
        this.input = input;
        com.alibaba.fastjson2.JSONReader.Context context = raw.getContext();
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
        try {
            return raw.read(type);
        } catch (com.alibaba.fastjson2.JSONException ex) {
            throw new JSONException(ex.getMessage(), ex.getCause());
        }
    }

    public Object readObject() {
        return raw.readObject();
    }

    public void readObject(Object object) {
        raw.readObject(object);
    }

    public Integer readInteger() {
        return raw.readInt32();
    }

    public Long readLong() {
        return raw.readInt64();
    }

    public String readString() {
        return raw.readString();
    }

    public boolean hasNext() {
        if (raw.isEnd()) {
            return false;
        }

        char ch = raw.current();
        return ch != ']' && ch != '}';
    }

    public Locale getLocal() {
        return raw.getContext().getLocale();
    }

    public TimeZone getTimeZone() {
        return raw.getContext().getTimeZone();
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
