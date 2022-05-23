package com.alibaba.fastjson2.hsf;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UCaseNameTest {
    @Test
    public void Bean_0() {
        Bean bean = new Bean();
        bean.THIS_IS_VALUE_1 = "Value1";
        bean.THIS_IS_VALUE_2 = "Value2";

        JSONWriter.Feature[] writeFeatures = {JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol};

        JSONReader.Feature[] readerFeatures = {JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased};

        byte[] jsonbBytes = JSONB.toBytes(bean, writeFeatures);

        Bean result = JSONB.parseObject(jsonbBytes, Bean.class, readerFeatures);

        assertEquals(bean.THIS_IS_VALUE_1, result.THIS_IS_VALUE_1);
        assertEquals(bean.THIS_IS_VALUE_2, result.THIS_IS_VALUE_2);
    }

    private static class Bean {
        private String THIS_IS_VALUE_1;
        private String THIS_IS_VALUE_2;
    }
}
