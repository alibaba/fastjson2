package com.alibaba.fastjson2.arraymapping;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EishayTest {
    static final JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
//            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol,
            JSONWriter.Feature.BeanToArray
    };

    static final JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.SupportArrayToBean,
            JSONReader.Feature.FieldBased
    };

    Object object;

    @BeforeEach
    public void init() {
        URL url = this.getClass().getClassLoader().getResource("data/eishay.json");
        object = JSON.parseObject(url, MediaContent.class);
    }

    @Test
    public void test() {
        byte[] bytes = JSONB.toBytes(object, writerFeatures);
        Object object1 = JSONB.parseObject(bytes, Object.class, readerFeatures);
        assertEquals(object, object1);
    }
}
