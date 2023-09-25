package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class Issue1874 {
    @Test
    public void test() {
        List<TestJson> list = new ArrayList<>();
        list.add(new TestJson(true, Boolean.FALSE));
        list.add(new TestJson(false, Boolean.TRUE));
        String json = JSON.toJSONString(list, JSONWriter.Feature.WriteBooleanAsNumber);
        assertEquals("[{\"b\":1,\"b2\":0},{\"b\":0,\"b2\":1}]", json);
        List<TestJson> list2 = JSON.parseArray(json, TestJson.class);
        assertEquals(list, list2);
    }

    @Data
    @AllArgsConstructor
    public static class TestJson {
        private boolean b;
        private Boolean b2;
    }
}
