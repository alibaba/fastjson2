package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class JSONTest {
    @Test
    public void test() {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        assertEquals(HashMap.class, JSON.parseObject(bytes).getInnerMap().getClass());
        assertEquals(LinkedHashMap.class, JSON.parseObject(bytes, Feature.OrderedField).getInnerMap().getClass());
    }

    @Test
    public void test1() {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        assertEquals(HashMap.class, JSON.parseObject(str).getInnerMap().getClass());
        assertEquals(LinkedHashMap.class, JSON.parseObject(str, Feature.OrderedField).getInnerMap().getClass());
    }

    @Test
    public void testNull() throws Exception {
        assertNull(JSON.parseObject((InputStream) null, Object.class));
        assertNull(JSON.parseObject((InputStream) null, (Type) Object.class));
    }

    @Test
    public void testInputStream() throws Exception {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        Bean bean = JSON.parseObject(new ByteArrayInputStream(utf8), Bean.class);
        assertEquals(123, bean.id);
        assertEquals("wenshao", bean.name);
    }

    @Test
    public void testCharArray() throws Exception {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        Bean bean = JSON.parseObject(str.toCharArray(), Bean.class);
        assertEquals(123, bean.id);
        assertEquals("wenshao", bean.name);
    }

    @Test
    public void toJSONString() {
        Bean bean = new Bean();
        bean.id = 123;
        assertEquals(
                "{\"id\":123}",
                JSON.toJSONString(bean, null, null, new SerializeFilter[0])
        );
    }

    public static class Bean {
        public int id;
        public String name;
    }

    @Test
    public void toJSONString1() {
        BeanAware bean = new BeanAware(123);
        assertEquals("123", JSON.toJSONString(bean));

        {
            ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(BeanAware.class);
            JSONWriter jsonWriter = JSONWriter.of();
            objectWriter.write(jsonWriter, null, null, null, 0);
            assertEquals("null", jsonWriter.toString());
        }

        {
            ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(BeanAware.class);
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            objectWriter.writeJSONB(jsonWriter, null, null, null, 0);
            byte[] bytes = jsonWriter.getBytes();
            assertNull(JSONB.parse(bytes));
        }
    }

    public static class BeanAware
            implements JSONAware {
        private int id;

        public BeanAware(int id) {
            this.id = id;
        }

        @Override
        public String toJSONString() {
            return Integer.toString(id);
        }
    }

    @Test
    public void isProxy() {
        assertFalse(TypeUtils.isProxy(Object.class));
    }
}
