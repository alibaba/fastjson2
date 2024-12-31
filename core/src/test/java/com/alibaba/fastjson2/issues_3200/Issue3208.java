package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class Issue3208 {
    @Test
    public void testArray() {
        byte[] bytes = new byte[]{1, 2, 3};
        String jsonString = JSON.toJSONString(bytes, SerializerFeature.WriteClassName);
        byte[] bytes1 = (byte[]) JSON.parse(jsonString);
        Assertions.assertTrue(Arrays.equals(bytes, bytes1));
    }

    @Test
    public void testObjectArray() {
        byte[] jsonBytes = JSON.toJSONBytes(new Object[]{JSON.toJSONString(ImmutableMap.of("a", 1))}, new SerializeConfig(true), JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.WriteClassName);
        Object parsed = JSON.parse(new String(jsonBytes));
        Assertions.assertNotNull(parsed);
    }
}
