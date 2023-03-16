package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1241 {
    @Test
    public void testTrue() {
        Bean bean = new Bean();
        bean.value = true;
        assertEquals("{\"value\":1}", JSON.toJSONString(bean, JSONWriter.Feature.WriteBooleanAsNumber));
        assertEquals("{\"value\":1}", new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteBooleanAsNumber)));
    }

    @Test
    public void testFalse() {
        Bean bean = new Bean();
        bean.value = false;
        assertEquals("{\"value\":0}", JSON.toJSONString(bean, JSONWriter.Feature.WriteBooleanAsNumber));
        assertEquals("{\"value\":0}", new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteBooleanAsNumber)));
    }

    public static class Bean {
        public boolean value;
    }

    @Test
    public void testTrue1() {
        Bean1 bean = new Bean1();
        bean.value = true;
        assertEquals("{\"value\":1}", JSON.toJSONString(bean, JSONWriter.Feature.WriteBooleanAsNumber));
        assertEquals("{\"value\":1}", new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteBooleanAsNumber)));
    }

    @Test
    public void testFalse1() {
        Bean1 bean = new Bean1();
        bean.value = false;
        assertEquals("{\"value\":0}", JSON.toJSONString(bean, JSONWriter.Feature.WriteBooleanAsNumber));
        assertEquals("{\"value\":0}", new String(JSON.toJSONBytes(bean, JSONWriter.Feature.WriteBooleanAsNumber)));
    }

    public static class Bean1 {
        public Boolean value;
    }

    @Test
    public void testTrue2() {
        Bean2 bean = new Bean2();
        bean.value = true;
        assertEquals("{\"value\":1}", JSON.toJSONString(bean));
        assertEquals("{\"value\":1}", new String(JSON.toJSONBytes(bean)));
    }

    @Test
    public void testFalse2() {
        Bean2 bean = new Bean2();
        bean.value = false;
        assertEquals("{\"value\":0}", JSON.toJSONString(bean));
        assertEquals("{\"value\":0}", new String(JSON.toJSONBytes(bean)));
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.WriteBooleanAsNumber)
    public static class Bean2 {
        public boolean value;
    }
}
