package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Date;

import static com.alibaba.fastjson2.time.ZoneId.DEFAULT_ZONE_ID;
import static org.junit.jupiter.api.Assertions.*;

public class DubboTest7 {
    static final JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ErrorOnNoneSerializable,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };
    static final JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.ErrorOnNoneSerializable,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased,
            JSONReader.Feature.IgnoreAutoTypeNotMatch
    };

    @Test
    public void test() {
        Date date = new Date();

        byte[] jsonbBytes = JSONB.toBytes(date, writerFeatures);

        String str = JSONB.parseObject(
                jsonbBytes, String.class, readerFeatures
        );
        assertEquals(DateUtils.toString(date.getTime(), false, DEFAULT_ZONE_ID), str);
    }

    @Test
    public void test1() {
        Bean object = new Bean();
        object.id = 1001;
        byte[] jsonbBytes = JSONB.toBytes(object, writerFeatures);
        String str = JSONB.parseObject(
                jsonbBytes, String.class, readerFeatures
        );
        assertEquals(JSON.toJSONString(object), str);
    }

    public static class Bean
            implements Serializable {
        public int id;
    }

    @Test
    public void test2() {
        Bean bean = new Bean();
        bean.id = 1001;
        byte[] jsonbBytes = JSONB.toBytes(bean, writerFeatures);
        Bean1 bean1 = JSONB.parseObject(
                jsonbBytes, Bean1.class, readerFeatures
        );
        assertEquals(JSON.toJSONString(bean.id), bean1.id);
    }

    public static class Bean1
            implements Serializable {
        public String id;
    }

    @Test
    public void test3() {
        String str = JSONB.parseObject(
                JSONB.toBytes(new RuntimeException(), writerFeatures),
                String.class,
                readerFeatures
        );
        assertNotNull(str);
        JSONObject object = JSON.parseObject(str);
        assertEquals("RuntimeException", object.get("@type"));
    }

    static class A
            implements Serializable {
        public String a;
    }

    static class B
            implements Serializable {
        public String b;
    }

    static class A1
            implements Serializable {
        public String a;
    }

    static class B1
            implements Serializable {
        public String b;
    }

    @Test
    public void test4() {
        byte[] jsonbBytes = JSONB.toBytes(new A(), writerFeatures);
        ContextAutoTypeBeforeHandler typeFilter = new ContextAutoTypeBeforeHandler(this.getClass().getName());
        Object obj = JSONB.parseObject(jsonbBytes, B.class, typeFilter, readerFeatures);
        assertEquals(B.class, obj.getClass());
    }

    @Test
    public void test5() {
        byte[] jsonbBytes = JSONB.toBytes(new A1(), writerFeatures);
        ContextAutoTypeBeforeHandler typeFilter = new ContextAutoTypeBeforeHandler(this.getClass().getName());
        assertThrows(Exception.class,
                () -> JSONB.parseObject(
                        jsonbBytes,
                        B1.class,
                        typeFilter,
                        JSONReader.Feature.SupportAutoType,
                        JSONReader.Feature.UseDefaultConstructorAsPossible,
                        JSONReader.Feature.ErrorOnNoneSerializable,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased
                )
        );
    }
}
