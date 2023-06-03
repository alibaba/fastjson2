package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotWriteSetClassName {
    @Test
    public void test() {
        Set set = new HashSet();
        assertEquals("Set[]", JSON.toJSONString(set, JSONWriter.Feature.WriteClassName));
        assertEquals("[]",
                JSON.toJSONString(
                        set,
                        JSONWriter.Feature.WriteClassName,
                        JSONWriter.Feature.NotWriteSetClassName
                )
        );
    }
}
