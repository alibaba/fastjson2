package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubboTest8 {

    @Test
    void test(){
        JSONObject json = null;
        byte[] t = JSONB.toBytes(json, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        );
        JSONObject result = JSONB.parseObject(t, JSONObject.class, new ContextAutoTypeBeforeHandler(true, ""),
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        );
        assertEquals(json, result);
    }

}
