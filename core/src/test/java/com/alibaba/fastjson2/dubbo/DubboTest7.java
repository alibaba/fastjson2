package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        Date object = new Date();

        byte[] jsonbBytes = JSONB.toBytes(object, writerFeatures);

        String str = JSONB.parseObject(
                jsonbBytes, String.class, readerFeatures
        );
        assertEquals(DateUtils.toString(object), str);
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
}
