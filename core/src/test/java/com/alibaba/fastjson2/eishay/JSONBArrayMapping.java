package com.alibaba.fastjson2.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBArrayMapping {
    static final JSONWriter.Feature[] jsonbWriteFeaturesArrayMapping = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol,
            JSONWriter.Feature.BeanToArray
    };

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
    public void test() {
        byte[] bytes = JSONB.toBytes(object, jsonbWriteFeaturesArrayMapping);
        System.out.println(JSONB.toJSONString(bytes));
        Object object1 = JSONB.parseObject(bytes, Object.class, jsonbReaderFeaturesSupportBeanArray);

        assertEquals(
                JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat),
                JSON.toJSONString(object1, JSONWriter.Feature.PrettyFormat)
        );
    }
}
