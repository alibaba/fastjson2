package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IgnoreEmptyTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.name = "";

        assertEquals("{\"name\":\"\"}", JSON.toJSONString(bean));
        assertEquals("{}", JSON.toJSONString(bean, JSONWriter.Feature.IgnoreEmpty));
    }

    @Test
    public void testReflect() {
        Bean bean = new Bean();
        bean.name = "";

        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);
        assertEquals("{\"name\":\"\"}", objectWriter.toJSONString(bean));
        assertEquals("{}", objectWriter.toJSONString(bean, JSONWriter.Feature.IgnoreEmpty));
    }

    public static class Bean {
        public String name;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.name = "";

        ObjectWriter objectWriter = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean1.class);
        assertEquals("{\"name\":\"\"}", objectWriter.toJSONString(bean));
        assertEquals("{}", objectWriter.toJSONString(bean, JSONWriter.Feature.IgnoreEmpty));
    }

    public static class Bean1 {
        private String name;

        public String getName() {
            return name;
        }
    }
}
