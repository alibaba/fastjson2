package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SetTest {
    @Test
    public void test0() throws Exception {
        Bean bean = new Bean();
        bean.content = new HashSet<>();

        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"@type\":\"com.alibaba.fastjson2.autoType.SetTest$Bean\",\"content\":Set[]}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertNotNull(bean1.content);
        assertEquals(0, bean1.content.size());
    }

    public static class Bean {
        public Collection<String> content;
    }

    @Test
    public void test1() throws Exception {
        String str = "{\"content\":Set[]}";
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertNotNull(bean1.content);
        assertEquals(0, bean1.content.size());
    }

    @Test
    public void test2() throws Exception {
        String str = "{\"content\":Set[]}";
        JSONObject object = JSON.parseObject(str);
        assertNotNull(object.get("content"));
        assertEquals(0, object.getJSONArray("content").size());
    }

    @Test
    public void test2Bytes() throws Exception {
        String str = "{\"content\":Set[]}";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONObject object = JSON.parseObject(bytes);
            assertNotNull(object.get("content"));
            assertEquals(0, object.getJSONArray("content").size());
        }
        {
            JSONObject object = JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII);
            assertNotNull(object.get("content"));
            assertEquals(0, object.getJSONArray("content").size());
        }
    }
}
