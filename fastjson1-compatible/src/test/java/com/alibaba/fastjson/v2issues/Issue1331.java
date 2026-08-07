package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1331 {
    @Test
    public void test() {
        List list = new ArrayList();
        list.add(101);
        list.add(102L);

        String str = JSON.toJSONString(list, SerializerFeature.WriteNonStringValueAsString);
        assertEquals("[\"101\",\"102\"]", str);
    }
}
