package com.alibaba.fastjson2.eishay;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.IdentityHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBArrayMapping {
    static final JSONReader.Feature[] jsonbReaderFeaturesSupportBeanArray = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.SupportArrayToBean,
    };

    String str;
    MediaContent object;

    @BeforeEach
    public void init() {
        URL url = this.getClass().getClassLoader().getResource("data/eishay.json");
        object = JSON.parseObject(url, MediaContent.class);
        str = JSON.toJSONString(object);
    }

    @Test
    public void test() throws Exception {
        final JSONWriter.Feature[] jsonbWriteFeaturesArrayMapping = {
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol,
                JSONWriter.Feature.BeanToArray
        };

        JSONWriter writer = JSONWriter.ofJSONB(jsonbWriteFeaturesArrayMapping);
        JSONWriter.Context context = writer.getContext();

        writer.setRootObject(object);
        Class<?> valueClass = object.getClass();
        ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
        objectWriter.writeArrayMappingJSONB(writer, object, null, null, 0);

        Field field = JSONWriter.class.getDeclaredField("refs");
        field.setAccessible(true);
        IdentityHashMap refs = (IdentityHashMap) field.get(writer);

        assertEquals(5, refs.size());

        byte[] bytes = writer.getBytes();
        Object object1 = JSONB.parseObject(bytes, Object.class, jsonbReaderFeaturesSupportBeanArray);

        assertEquals(
                JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat),
                JSON.toJSONString(object1, JSONWriter.Feature.PrettyFormat)
        );
    }

    @Test
    public void test1() throws Exception {
        final JSONWriter.Feature[] jsonbWriteFeatures = {
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        };

        JSONWriter writer = JSONWriter.ofJSONB(jsonbWriteFeatures);
        JSONWriter.Context context = writer.getContext();

        writer.setRootObject(object);
        Class<?> valueClass = object.getClass();
        ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
        objectWriter.writeArrayMappingJSONB(writer, object, null, null, 0);

        Field field = JSONWriter.class.getDeclaredField("refs");
        field.setAccessible(true);
        IdentityHashMap refs = (IdentityHashMap) field.get(writer);

        assertEquals(5, refs.size());

        byte[] bytes = writer.getBytes();
        Object object1 = JSONB.parseObject(bytes, Object.class, jsonbReaderFeaturesSupportBeanArray);

        assertEquals(
                JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat),
                JSON.toJSONString(object1, JSONWriter.Feature.PrettyFormat)
        );
    }
}
