package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import org.junit.Assert;
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
        Map<Integer, List<Integer>> o = JSONB.parseObject(bytes, parameterizedType);
        Integer i = o.get(2).get(0);
        Assert.assertEquals(-1, (int) i);
    }
}
