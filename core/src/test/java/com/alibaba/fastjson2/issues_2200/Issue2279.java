package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.Test;

import java.util.*;

public class Issue2279 {
    @Test
    public void test01() {
        Map<Integer, List<Integer>> result = new HashMap<>();
        result.put(1, Arrays.asList(-1));
        result.put(2, Arrays.asList(-1));

        byte[] bytes = JSONB.toBytes(result, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ErrorOnNoneSerializable, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NotWriteDefaultValue, JSONWriter.Feature.NotWriteHashMapArrayListClassName, JSONWriter.Feature.WriteNameAsSymbol);
        ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(Map.class, Integer.class, new ParameterizedTypeImpl(List.class, Integer.class));
        Object o = JSONB.parseObject(bytes, parameterizedType);
        System.out.println(JSONObject.toJSONString(o));
    }
    @Test
    public void test02() {
        List<Integer> list = Arrays.asList(1, 1);

        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ErrorOnNoneSerializable, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NotWriteDefaultValue, JSONWriter.Feature.NotWriteHashMapArrayListClassName, JSONWriter.Feature.WriteNameAsSymbol);
        ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(List.class, Integer.class);
        Object o = JSONB.parseObject(bytes, parameterizedType);
        System.out.println(JSONObject.toJSONString(o));
    }
}
