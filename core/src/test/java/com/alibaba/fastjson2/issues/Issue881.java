package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue881 {

    @Test
    public void fastjson2Test() {
        TestClass<List<String>> testClass = new TestClass<>();
        JSON.config(JSONWriter.Feature.WriteNullListAsEmpty, JSONWriter.Feature.WriteNulls);
        System.out.println();
        assertEquals("{\"str\":null,\"stringList\":[]}",
                JSON.toJSONString(testClass));
    }

    @Getter
    @Setter
    static class TestClass<T> {
        private T stringList;
        private String str;
    }
}
