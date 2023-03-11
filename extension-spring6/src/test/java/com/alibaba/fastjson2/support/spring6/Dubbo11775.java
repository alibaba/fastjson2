package com.alibaba.fastjson2.support.spring6;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.DefaultAopProxyFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Dubbo11775 {
    @Test
    public void test() {
        ParamsDTO paramsDTO = new ParamsDTO();
        ParamsItemDTO paramsItemDTO = new ParamsItemDTO();
        paramsItemDTO.setA("aaa");
        paramsDTO.setParamsItems(Arrays.asList(paramsItemDTO));

        AdvisedSupport config = new AdvisedSupport();
        config.setTarget(paramsDTO);
        DefaultAopProxyFactory factory = new DefaultAopProxyFactory();
        Object proxy = factory.createAopProxy(config).getProxy();
        Object proxy1 = factory.createAopProxy(config).getProxy();

        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(proxy.getClass());
        ObjectWriter objectWriter1 = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(proxy1.getClass());
        assertSame(objectWriter, objectWriter1);

        byte[] jsonbBytes = JSONB.toBytes(proxy, writerFeatures);
        System.out.println(JSONB.toJSONString(jsonbBytes));
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
