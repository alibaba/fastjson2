package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONWriterTest {
    @Test
    public void config() {
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
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
}
