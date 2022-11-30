package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.util.List;

public class JSONSerializer {
    public final SerializeWriter out;
    final JSONWriter raw;
    SerialContext context;

    public JSONSerializer() {
        this(new SerializeWriter());
    }

    public JSONSerializer(SerializeConfig config) {
        this(new SerializeWriter(config));
    }

    public JSONSerializer(JSONWriter raw) {
        this(new SerializeWriter(raw));
    }

    public JSONSerializer(SerializeWriter out) {
        this.out = out;
        this.raw = out.raw;
    }

    public JSONSerializer(SerializeWriter out, SerializeConfig config) {
        this.out = out;
        this.raw = out.raw;
    }

    public void config(SerializerFeature feature, boolean state) {
        if (!state) {
            throw new JSONException("not support");
        }

        JSONWriter.Context ctx = raw.getContext();

        switch (feature) {
            case UseISO8601DateFormat:
                ctx.setDateFormat("iso8601");
                break;
            case WriteMapNullValue:
                ctx.config(JSONWriter.Feature.WriteNulls);
                break;
            case WriteNullListAsEmpty:
                ctx.config(JSONWriter.Feature.WriteNullListAsEmpty);
                break;
            case WriteNullStringAsEmpty:
                ctx.config(JSONWriter.Feature.WriteNullStringAsEmpty);
                break;
            case WriteNullNumberAsZero:
                ctx.config(JSONWriter.Feature.WriteNullNumberAsZero);
                break;
            case WriteNullBooleanAsFalse:
                ctx.config(JSONWriter.Feature.WriteNullBooleanAsFalse);
                break;
            case BrowserCompatible:
                ctx.config(JSONWriter.Feature.BrowserCompatible);
                break;
            case BrowserSecure:
                ctx.config(JSONWriter.Feature.BrowserSecure);
                break;
            case WriteClassName:
                ctx.config(JSONWriter.Feature.WriteClassName);
                break;
            case WriteNonStringValueAsString:
                ctx.config(JSONWriter.Feature.WriteNonStringValueAsString);
                break;
            case WriteEnumUsingToString:
                ctx.config(JSONWriter.Feature.WriteEnumUsingToString);
                break;
            case NotWriteRootClassName:
                ctx.config(JSONWriter.Feature.NotWriteRootClassName);
                break;
            case IgnoreErrorGetter:
                ctx.config(JSONWriter.Feature.IgnoreErrorGetter);
                break;
            case WriteDateUseDateFormat:
                ctx.setDateFormat(JSON.DEFFAULT_DATE_FORMAT);
                break;
            case BeanToArray:
                ctx.config(JSONWriter.Feature.BeanToArray);
                break;
            case UseSingleQuotes:
                ctx.config(JSONWriter.Feature.UseSingleQuotes);
                break;
            default:
                break;
        }
    }

    public void write(boolean value) {
        raw.writeBool(value);
    }

    public void writeInt(int i) {
        raw.writeInt32(i);
    }

    public void write(String text) {
        raw.writeString(text);
    }

    public void writeLong(long i) {
        raw.writeInt64(i);
    }

    public void writeNull() {
        raw.writeNull();
    }

    public final void write(Object object) {
        raw.writeAny(object);
    }

    public final void writeAs(Object object, Class type) {
        ObjectWriter objectWriter = raw.getObjectWriter(type);
        objectWriter.write(raw, 0);
    }

    @Override
    public String toString() {
        return this.raw.toString();
    }

    public List<PropertyFilter> getPropertyFilters() {
        return this.out.getPropertyFilters();
    }

    public List<ValueFilter> getValueFilters() {
        return this.out.getValueFilters();
    }
    public List<NameFilter> getNameFilters() {
        return this.out.getNameFilters();
    }

    public List<BeforeFilter> getBeforeFilters() {
        return this.out.getBeforeFilters();
    }

    public List<AfterFilter> getAfterFilters() {
        return this.out.getAfterFilters();
    }

    public SerializeConfig getMapping() {
        return out.config;
    }

    public SerializeWriter getWriter() {
        return out;
    }

    public ObjectSerializer getObjectWriter(Class<?> clazz) {
        return out.config.getObjectWriter(clazz);
    }

    public static void write(SerializeWriter out, Object object) {
        out.raw.writeAny(object);
    }

    public SerialContext getContext() {
        return context;
    }

    public void setContext(SerialContext context) {
        this.context = context;
    }
}
