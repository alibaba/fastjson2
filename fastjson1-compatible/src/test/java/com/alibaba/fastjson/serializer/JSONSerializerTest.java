package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSONSerializerTest {
    @Test
    public void config() {
        JSONSerializer writer = new JSONSerializer();
        writer.config(SerializerFeature.UseISO8601DateFormat, true);

        com.alibaba.fastjson2.JSONWriter.Context context = writer.raw.getContext();
        assertEquals("iso8601", context.getDateFormat());

        writer.config(SerializerFeature.WriteMapNullValue, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.WriteNulls));

        writer.config(SerializerFeature.WriteNullListAsEmpty, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.WriteNullListAsEmpty));

        writer.config(SerializerFeature.WriteNullStringAsEmpty, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.WriteNullStringAsEmpty));

        writer.config(SerializerFeature.WriteNullNumberAsZero, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.WriteNullNumberAsZero));

        writer.config(SerializerFeature.WriteNullBooleanAsFalse, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.WriteNullBooleanAsFalse));

        writer.config(SerializerFeature.BrowserCompatible, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.BrowserCompatible));

        writer.config(SerializerFeature.WriteClassName, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.WriteClassName));

        writer.config(SerializerFeature.WriteNonStringValueAsString, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString));

        writer.config(SerializerFeature.WriteEnumUsingToString, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.WriteEnumUsingToString));

        writer.config(SerializerFeature.NotWriteRootClassName, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.NotWriteRootClassName));

        writer.config(SerializerFeature.IgnoreErrorGetter, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.IgnoreErrorGetter));

        writer.config(SerializerFeature.BeanToArray, true);
        assertTrue(context.isEnabled(com.alibaba.fastjson2.JSONWriter.Feature.BeanToArray));

        writer.config(SerializerFeature.WriteDateUseDateFormat, true);
        assertEquals(JSON.DEFFAULT_DATE_FORMAT, context.getDateFormat());
    }

    @Test
    public void write() {
        JSONSerializer writer = new JSONSerializer();
        writer.write(true);
        assertEquals("true", writer.toString());
    }

    @Test
    public void writeInt() {
        JSONSerializer writer = new JSONSerializer();
        writer.writeInt(1086);
        assertEquals("1086", writer.toString());
    }

    @Test
    public void writeLong() {
        JSONSerializer writer = new JSONSerializer();
        writer.writeLong(2155L);
        assertEquals("2155", writer.toString());
    }

    @Test
    public void writeNull() {
        JSONSerializer writer = new JSONSerializer(SerializeConfig.global);
        assertSame(SerializeConfig.global, writer.getMapping());
        assertNotNull(writer.getWriter());
        writer.writeNull();
        assertEquals("null", writer.toString());
    }

    @Test
    public void getBeforeFilters() {
        JSONSerializer writer = new JSONSerializer();
        assertEquals(0, writer.getBeforeFilters().size());
    }
}
