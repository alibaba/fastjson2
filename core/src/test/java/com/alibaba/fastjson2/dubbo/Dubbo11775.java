package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Dubbo11775 {
    @Test
    public void test() {
        ParamsDTO paramsDTO = new ParamsDTO();
        ParamsItemDTO paramsItemDTO = new ParamsItemDTO();
        paramsItemDTO.setA("aaa");
        paramsDTO.setParamsItems(Arrays.asList(paramsItemDTO));

        byte[] jsonbBytes = JSONB.toBytes(paramsDTO, writerFeatures);
        ParamsDTO paramsDTO1 = (ParamsDTO) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);
        assertEquals(paramsDTO.paramsItems.getClass(), paramsDTO1.paramsItems.getClass());
        assertEquals(paramsDTO.paramsItems.size(), paramsDTO1.paramsItems.size());
        assertEquals(paramsDTO.paramsItems.get(0).a, paramsDTO1.paramsItems.get(0).a);
    }

    @Data
    public static class ParamsItemDTO
            implements Serializable {
        private String a;
    }

    @Data
    public static class ParamsDTO
            implements Serializable {
        private List<ParamsItemDTO> paramsItems;
    }

    JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ErrorOnNoneSerializable,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };

    JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.ErrorOnNoneSerializable,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };
}
