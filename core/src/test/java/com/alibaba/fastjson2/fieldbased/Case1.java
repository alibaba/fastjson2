package com.alibaba.fastjson2.fieldbased;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Case1 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.userId = 101;
        bean.UserId = 102;

        String str = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased);
        assertEquals("{\"UserId\":102,\"userId\":101}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertNotNull(bean1);
        assertEquals(bean.userId, bean1.userId);
        assertEquals(bean.UserId, bean1.UserId);
    }

    @Test
    public void test_arrayMapping() {
        Bean bean = new Bean();
        bean.userId = 101;
        bean.UserId = 102;

        String str = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray);
        assertEquals("[102,101]", str);
        Bean bean1 = JSON.parseObject(str, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertNotNull(bean1);
        assertEquals(bean.userId, bean1.userId);
        assertEquals(bean.UserId, bean1.UserId);
    }

    @Test
    public void test_jsonb() {
        Bean bean = new Bean();
        bean.userId = 101;
        bean.UserId = 102;

        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.FieldBased);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class);
        assertNotNull(bean1);
        assertEquals(bean.userId, bean1.userId);
        assertEquals(bean.UserId, bean1.UserId);
    }

    @Test
    public void test_jsonb_arrayMapping() {
        Bean bean = new Bean();
        bean.userId = 101;
        bean.UserId = 102;

        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class, JSONReader.Feature.SupportArrayToBean);
        assertNotNull(bean1);
        assertEquals(bean.userId, bean1.userId);
        assertEquals(bean.UserId, bean1.UserId);
    }

    @Test
    public void test_jsonb_arrayMapping_auoType() {
        Bean bean = new Bean();
        bean.userId = 101;
        bean.UserId = 102;

        byte[] jsonbBytes = JSONB.toBytes(bean, JSONWriter.Feature.FieldBased, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.WriteClassName);
        Bean bean1 = JSONB.parseObject(jsonbBytes, Bean.class, JSONReader.Feature.SupportArrayToBean, JSONReader.Feature.SupportAutoType);
        assertNotNull(bean1);
        assertEquals(bean.userId, bean1.userId);
        assertEquals(bean.UserId, bean1.UserId);
    }

    public static class Bean {
        public int userId;
        public Integer UserId;
    }
}
