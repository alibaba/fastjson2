package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompactStringsTest {
    @Test
    public void test() {
        byte[] bytes = {
                104, 99, 111, 109, 46, 113, 105, 121, 105, 46, 100, 117, 98, 98, 111, 97, 112, 105, 46, 83, 116, 114, 101, 97, 109, 83, 101, 114, 118, 105, 99, 101
        };

        String str = JSONB.parseObject(
                bytes,
                String.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        );
        assertEquals("com.qiyi.dubboapi.StreamService", str);
    }
}
