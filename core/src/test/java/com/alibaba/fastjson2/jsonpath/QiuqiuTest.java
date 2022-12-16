package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QiuqiuTest {
    @Test
    public void test() {
        CsiObject csiObject = new CsiObject();
        JSONPath.set(csiObject, "$.csiLive[0].id", "123");
        assertEquals("{\"csiLive\":[{\"id\":\"123\"}]}", JSON.toJSONString(csiObject));
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsiObject {
        private List<CsiLive> csiLive;
    }

    public static class CsiLive {
        public int id;
    }

    @Test
    public void test1() {
        Root1 csiObject = new Root1();
        JSONPath.set(csiObject, "$.live.id", "123");
        assertEquals("{\"live\":{\"id\":123}}", JSON.toJSONString(csiObject));
    }

    public static class Root1 {
        public CsiLive live;
    }

    @Test
    public void test2() {
        Root2 csiObject = new Root2();
        JSONPath.set(csiObject, "$.item.live.id", "123");
        assertEquals("{\"item\":{\"live\":{\"id\":123}}}", JSON.toJSONString(csiObject));
    }

    public static class Root2 {
        public Item item;
    }

    public static class Item {
        public CsiLive live;
    }
}
