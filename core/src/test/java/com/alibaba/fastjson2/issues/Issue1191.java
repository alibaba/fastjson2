package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1191 {
    @Test
    public void test() {
        String json = "[0, 1, 2, 3]";
        JSONPath path = JSONPath.of(new String[]{"$[3]", "$[4]"}, new Type[]{Long.class, Long.class});
        Object[] result = (Object[]) path.extract(json);
        assertArrayEquals(new Long[]{3L, null}, result);
    }

    @Test
    public void test1() {
        String json = "[0, 1, 2]";
        JSONPath path = JSONPath.of("$[3]", Integer.class);
        Object result = path.extract(json);
        assertNull(result);
    }

    @Test
    public void test2() {
        String json = "[]";
        JSONPath path = JSONPath.of("$[2]", Integer.class);
        Object result = path.extract(json);
        assertNull(result);
    }

    @Test
    public void test3() {
        JSONArray jsonArray = JSONArray.of(0, 1, 2, 3);
        byte[] bytes = JSONB.toBytes(jsonArray);
        JSONReader jsonReader = JSONReader.ofJSONB(bytes);
        JSONPath path = JSONPath.of("$[5]", String.class);
        Object result = path.extract(jsonReader);
        assertNull(result);
    }
}
