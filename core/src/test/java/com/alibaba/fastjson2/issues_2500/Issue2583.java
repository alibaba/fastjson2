package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson.parser.Feature.SupportAutoType;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2583 {
    @Test
    public void testAll() {
        Bean bean = new Bean();
        bean.enumMap = new EnumMap(TimeUnit.class);

        byte[] bytes = JSONB.toBytes(bean);
        Bean bean1 = (Bean) JSONB.parseObject(bytes, Bean.class);
        assertEquals(0, bean1.enumMap.size());

        String json = JSON.toJSONString(bean);
        Bean bean2 = (Bean) JSON.parseObject(json, Bean.class);
        assertEquals(0, bean2.enumMap.size());

        byte[] bytes2 = JSON.toJSONString(bean).getBytes(StandardCharsets.UTF_8);
        Bean bean3 = (Bean) JSON.parseObject(bytes2, Bean.class);
        assertEquals(0, bean3.enumMap.size());

        String json2 = com.alibaba.fastjson.JSON.toJSONString(bean);
        Bean bean4 = (Bean) com.alibaba.fastjson.JSON.parseObject(json2, Bean.class);
        assertEquals(0, bean4.enumMap.size());
    }

    @Test
    public void test_jsonb() {
        Bean bean = new Bean();
        bean.enumMap = new EnumMap(TimeUnit.class);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean bean1 = (Bean) JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportAutoType);
        assertEquals(0, bean1.enumMap.size());
    }

    @Test
    public void test_json() {
        Bean bean = new Bean();
        bean.enumMap = new EnumMap(TimeUnit.class);

        String json = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        System.out.println(json);
        Bean bean1 = (Bean) JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportAutoType);
        assertEquals(0, bean1.enumMap.size());
    }

    @Test
    public void test_json_getBytes() {
        Bean bean = new Bean();
        bean.enumMap = new EnumMap(TimeUnit.class);

        byte[] bytes = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName).getBytes(StandardCharsets.UTF_8);
        Bean bean1 = (Bean) JSON.parseObject(bytes, Bean.class, JSONReader.Feature.SupportAutoType);
        assertEquals(0, bean1.enumMap.size());
    }

    @Test
    public void test_json_fj() {
        Bean bean = new Bean();
        bean.enumMap = new EnumMap(TimeUnit.class);

        String json = com.alibaba.fastjson.JSON.toJSONString(bean, SerializerFeature.WriteClassName);
        Bean bean1 = (Bean) com.alibaba.fastjson.JSON.parseObject(json, Bean.class, SupportAutoType);
        assertEquals(0, bean1.enumMap.size());
    }

    @Test
    public void test_json_getBytes_fj() {
        Bean bean = new Bean();
        bean.enumMap = new EnumMap(TimeUnit.class);

        byte[] bytes = com.alibaba.fastjson.JSON.toJSONString(bean, SerializerFeature.WriteClassName).getBytes(StandardCharsets.UTF_8);
        Bean bean1 = (Bean) com.alibaba.fastjson.JSON.parseObject(bytes, Bean.class, SupportAutoType);
        assertEquals(0, bean1.enumMap.size());
    }

    public static class Bean {
        public Map<TimeUnit, Object> enumMap;
    }
}
