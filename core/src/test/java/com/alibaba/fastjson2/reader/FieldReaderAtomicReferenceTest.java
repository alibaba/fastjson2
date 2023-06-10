package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldReaderAtomicReferenceTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.value.set("123");

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"123\"}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.value.get(), bean1.value.get());
    }

    public static class Bean {
        public final AtomicReference<String> value = new AtomicReference<>();
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1(new AtomicReference<>("123"));

        String str = JSON.toJSONString(bean);
        assertEquals("{\"value\":\"123\"}", str);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.value.get(), bean1.value.get());
    }

    public static class Bean1 {
        public final AtomicReference<String> value;

        public Bean1(@JSONField(name = "value") AtomicReference<String> value) {
            this.value = value;
        }
    }
}
